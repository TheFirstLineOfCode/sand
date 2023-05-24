#include <iostream>
#include <string>

#include "api/audio_codecs/builtin_audio_encoder_factory.h"
#include "api/audio_codecs/builtin_audio_decoder_factory.h"
#include "api/video_codecs/builtin_video_encoder_factory.h"
#include "api/video_codecs/builtin_video_decoder_factory.h"
#include "api/create_peerconnection_factory.h"
#include "json/json.h"
#include "pc/peer_connection.h"
#include "modules/video_capture/video_capture.h"

#include "WebcamWebrtcPeer.h"

using namespace std;

const char nameCandidateSdpMid[] = "sdpMid";
const char nameCandidateSdpMLineIndex[] = "sdpMLineIndex";
const char nameCandidateSdp[] = "candidate";

void showTransceivers(webrtc::PeerConnectionInterface *peerConnectionInterface) {
	cout << "Show current transceivers:" << endl;

	webrtc::PeerConnection *peerConnection = (webrtc::PeerConnection *)peerConnectionInterface;
	std::vector<rtc::scoped_refptr<webrtc::RtpTransceiverInterface>> transceivers =
		peerConnection->GetTransceivers();

	if(transceivers.size() == 0) {
		cout << "No transceivers.";
		return;
	}

	for(int i = 0; i < transceivers.size(); i++) {
		//rtc::scoped_refptr<webrtc::RtpTransceiverProxyWithInternal<webrtc::RtpTransceiver>> transceiver = transceivers[i];
		rtc::scoped_refptr<webrtc::RtpTransceiverInterface> transceiver = transceivers[i];
		webrtc::RtpTransceiverDirection direction = transceiver->direction();

		std::string out = "Found a transceiver. It's media type: " +
			cricket::MediaTypeToString(transceiver->media_type()) +
			", It's mid: " + (transceiver->mid().has_value() ? transceiver->mid().value() : "null") +
			", Is it stopped: " + (transceiver->stopped() ? "true" : "false");

		if(direction == webrtc::RtpTransceiverDirection::kSendRecv) {
			out += ", It's direction: kSendRecv.";
		} else if(direction == webrtc::RtpTransceiverDirection::kSendOnly) {
			out += ", It's direction: kSendOnly.";
		} else if(direction == webrtc::RtpTransceiverDirection::kRecvOnly) {
			out += ", It's direction: kRecvOnly.";
		} else if(direction == webrtc::RtpTransceiverDirection::kInactive) {
			out += ", It's direction: kInactive.";
		} else {
			out += ", It's direction: kStopped.";
		}
		
		cout << out << endl;
	}
};

class DummySetSessionDescriptionObserver : public webrtc::SetSessionDescriptionObserver {
public:
	static rtc::scoped_refptr<DummySetSessionDescriptionObserver> Create() {
		return new rtc::RefCountedObject<DummySetSessionDescriptionObserver>();
	}

	virtual void OnSuccess() {
		cout << "Session description is set." << endl;
	}

	virtual void OnFailure(webrtc::RTCError error) {
		cout << "Set session descriptin failed. Error type: "<< ToString(error.type()) <<
			"Error message:" << error.message() << "." << endl;
	}
};

class CreateAnswerObserver: public webrtc::CreateSessionDescriptionObserver {
public:
	CreateAnswerObserver(cppnet::Handle handle, rtc::scoped_refptr<webrtc::PeerConnectionInterface> peerConnection) {
		this->handle = handle;
		this->peerConnection = peerConnection;
	}

	void OnSuccess(webrtc::SessionDescriptionInterface *sessionDescription) {
		std::string sdp;
		if(!sessionDescription->ToString(&sdp)) {
			cout << "Creating answer failed. Can't get answer SDP." << endl;
			// TODO Send error to native service client
			return;
		}

		cout << "Creating answer succeeded. Current signaling state of peer connection: " << peerConnection->signaling_state() << "." << endl;

		cout << "Answer SDP created. Answer SDP: " << sdp << ".";
		std::string answer = "ANSWER " + sdp;
		handle->Write(answer.c_str(), answer.size());

		// Waiting for the answer SDP reached to the peer.
		rtc::Thread::Current()->SleepMs(1000 * 5);

		peerConnection->SetLocalDescription(DummySetSessionDescriptionObserver::Create(),
			sessionDescription);
	}

	void OnFailure(webrtc::RTCError error) {
		cout << "Failed to create answer. Error message: " << error.message() << "." << endl;
		// TODO Send error to native service client
	}

private:
	cppnet::Handle handle;
	rtc::scoped_refptr<webrtc::PeerConnectionInterface> peerConnection;
};

class SetRemoteSessionDescriptionObserver: public webrtc::SetSessionDescriptionObserver {
public:
	SetRemoteSessionDescriptionObserver(cppnet::Handle handle,
			rtc::scoped_refptr<webrtc::PeerConnectionInterface> peerConnection) {
		this->handle = handle;
		this->peerConnection = peerConnection;
	}

	virtual void OnSuccess() {
		cout << "Creating remote session description succeeded. Current signaling state of peer connection: " << peerConnection->signaling_state() << "." << endl;

		cout << "Before creating answer." << endl;

		rtc::scoped_refptr<CreateAnswerObserver> creatAnswerObserver =
			new rtc::RefCountedObject<CreateAnswerObserver>(handle, peerConnection);
		peerConnection->CreateAnswer(creatAnswerObserver, webrtc::PeerConnectionInterface::RTCOfferAnswerOptions());

		cout << "After creating answer." << endl;
	}

	virtual void OnFailure(webrtc::RTCError error) {
		cout << "Set remote session descriptin failed. Error type: "<< ToString(error.type()) <<
			"Error message:" << error.message() << "." << endl;
	}

private:
	cppnet::Handle handle;
	rtc::scoped_refptr<webrtc::PeerConnectionInterface> peerConnection;
};

WebcamWebrtcPeer::WebcamWebrtcPeer(webrtc::PeerConnectionInterface::IceServers _iceServers) {
	videoCaptureDeviceName = nullptr;
	opened = false;

	videoCaptureDeviceName = nullptr;
	bestVideoCaptureCapability = nullptr;

	iceServers = _iceServers;
	
	signalingThread = rtc::Thread::Create();
}

void WebcamWebrtcPeer::setSocketHandle(cppnet::Handle _handle) {
	if (_handle.get())
		handle.reset();

	handle = _handle;
}

void WebcamWebrtcPeer::open(int requestedVideoCaptureWidth, int requestedVideoCaptureHeight,
			int requestedVideoCaptureMaxFps) {
	static const char openedMsg[] = "OPENED";

	if(opened) {
		handle->Write(openedMsg, sizeof(openedMsg));
		return;
	}

	if (!peerConnection.get()) {
		getBestVideoCaptureCapability(requestedVideoCaptureWidth, requestedVideoCaptureHeight,
			requestedVideoCaptureMaxFps);
		createPeerConnection();
		addVideoTrack();
	}

	opened = true;
	handle->Write(openedMsg, sizeof(openedMsg));
}

void WebcamWebrtcPeer::close() {
	static const char closedMsg[] = "CLOSED";

	if(!opened) {
		handle->Write(closedMsg, sizeof(closedMsg));
		return;
	}

	if (peerConnection.get()) {
		peerConnection->Close();
		peerConnection->Release();
		peerConnection.release();
	}

	if (peerConnectionFactory.get()) {
		peerConnectionFactory->Release();
		peerConnectionFactory.release();
		signalingThread->Stop();
	}

	if(videoDevice.get()) {
		videoDevice->Release();
		videoDevice.release();
	}

	opened = false;
	handle->Write(closedMsg,sizeof(closedMsg));
}

bool WebcamWebrtcPeer::isOpened() {
	return opened;
}

bool WebcamWebrtcPeer::isClosed() {
	return !opened;
}

void WebcamWebrtcPeer::createPeerConnection() {
	if (peerConnection.get())
		return;

	if (!peerConnectionFactory.get())
		createPeerConnectionFactory();

	webrtc::PeerConnectionInterface::RTCConfiguration configuration;
	configuration.sdp_semantics = webrtc::SdpSemantics::kUnifiedPlan;
	configuration.enable_dtls_srtp = true;
	configuration.disable_ipv6 = true;
	configuration.disable_ipv6_on_wifi = true;
	configuration.servers = iceServers;

	peerConnection = peerConnectionFactory->CreatePeerConnection(configuration, nullptr, nullptr, this);
}

void WebcamWebrtcPeer::addVideoTrack() {
	if (!peerConnection->GetSenders().empty()) {
		return;  // Track has already been added.
	}

	// TODO
	if (!videoDevice.get()) {
		videoDevice = CapturerTrackSource::Create(bestVideoCaptureCapability->width,
			bestVideoCaptureCapability->height, bestVideoCaptureCapability->maxFPS);

		if (!videoDevice.get()) {
			cout << "Error. Can't create capturer track source." << endl;
			// TODO Send error to native service client
			return;
		}
	}
	
	rtc::scoped_refptr<webrtc::VideoTrackInterface> videoTrack = peerConnectionFactory->CreateVideoTrack(labelVideoTrack, videoDevice);

	if(!videoTrack.get()) {
		// TODO Send error to native service client
		return;
	}

	webrtc::RTCErrorOr<rtc::scoped_refptr<webrtc::RtpSenderInterface>> errorOrSender = peerConnection->AddTrack(videoTrack, {"video_stream"});
	if (!errorOrSender.ok()) {
		cout << "Error occurred when adding video track to peer connection. Error type: " <<
			ToString(errorOrSender.error().type()) << "Error message:" << errorOrSender.error().message() << "." << endl;

		// TODO Send error to peer.
	}
}

void WebcamWebrtcPeer::offered(std::string offerSdp) {
	if(!opened) {
		static const char notOpenedError[] = "ERROR Not opened.";
		handle->Write(notOpenedError,sizeof(notOpenedError));

		return;
	}

	cout << "Enter to offered method. Current signaling state of peer connection: " << peerConnection->signaling_state() << "." << endl;

	webrtc::SdpType offerType = webrtc::SdpType::kOffer;
	webrtc::SdpParseError error;
	std::unique_ptr<webrtc::SessionDescriptionInterface> remoteSessionDescription =
		webrtc::CreateSessionDescription(offerType, offerSdp, &error);
	if(!remoteSessionDescription) {
		static const char offerNotParsed[] = "ERROR Can't parse offer SDP.";
		cout << "Can't parse offer SDP. Error: " << error.description << endl;
		handle->Write(offerNotParsed, sizeof(offerNotParsed));

		return;
	}

	cout << "Before setting remote session description." << endl;

	rtc::scoped_refptr<SetRemoteSessionDescriptionObserver> setRemoteSessionDescriptionObserver =
		new rtc::RefCountedObject<SetRemoteSessionDescriptionObserver>(handle, peerConnection);
	peerConnection->SetRemoteDescription(setRemoteSessionDescriptionObserver,
		remoteSessionDescription.release());
}

void WebcamWebrtcPeer::iceCandidateFound(std::string jsonCandidate) {
	cout << "ICE candidate found. Candidate is: " + jsonCandidate << endl;

	Json::Reader reader;
	Json::Value vCandidate;
	if(!reader.parse(jsonCandidate, vCandidate)) {
		cout << "Error. Can't parse ICE candidate message." << endl;
		return;
	}

	Json::Value vSdpMid = vCandidate[nameCandidateSdpMid];
	Json::Value vSdpMLineIndex = vCandidate[nameCandidateSdpMLineIndex];
	Json::Value vSdp = vCandidate[nameCandidateSdp];

	if(vSdpMid.isNull() || vSdpMLineIndex.isNull() || vSdp.isNull()) {
		cout << "Error. Can't parse ICE candidate message. Some key fields of ICE candate message are missed." << endl;
		return;
	}

	std::string sdpMid = vSdpMid.asString();
	int sdpMLineIndex = vSdpMLineIndex.asInt();
	std::string sdp = vSdp.asString();

	webrtc::SdpParseError error;
	std::unique_ptr<webrtc::IceCandidateInterface> candidate(webrtc::CreateIceCandidate(
		sdpMid, sdpMLineIndex, sdp, &error));

	if(!candidate.get()) {
		cout << "Can't parse received candidate message. " << "SdpParseError was: "
			<< error.description << endl;
		return;
	}

	if(!peerConnection->AddIceCandidate(candidate.get())) {
		cout << "Failed to apply the received candidate.";
		return;
	}

	cout << " Received candidate :" << jsonCandidate;
}

void WebcamWebrtcPeer::createPeerConnectionFactory() {
	if (peerConnectionFactory.get())
		return;

	signalingThread->Start();
	peerConnectionFactory = webrtc::CreatePeerConnectionFactory(
		nullptr /* network_thread */,
		nullptr /* worker_thread */,
		signalingThread.get(),
		nullptr /* default_adm */,
		webrtc::CreateBuiltinAudioEncoderFactory(),
		webrtc::CreateBuiltinAudioDecoderFactory(),
		webrtc::CreateBuiltinVideoEncoderFactory(),
		webrtc::CreateBuiltinVideoDecoderFactory(),
		nullptr /* audio_mixer */,
		nullptr /* audio_processing */
	);
}

const char *WebcamWebrtcPeer::getVideoCaptureDeviceName() {
	if (videoCaptureDeviceName)
		return videoCaptureDeviceName;

	webrtc::VideoCaptureModule::DeviceInfo *deviceInfo = webrtc::VideoCaptureFactory::CreateDeviceInfo();
	if (deviceInfo && deviceInfo->NumberOfDevices() > 0) {
		videoCaptureDeviceName = (char *)malloc(sizeof(char) * 256);
		char videoCaptureDeviceId[256];
		if (deviceInfo->GetDeviceName(0, videoCaptureDeviceName, 256, videoCaptureDeviceId, 256) != 0) {
			delete videoCaptureDeviceName;
			videoCaptureDeviceName = nullptr;
		} else {
			cout << "Video capture named '" << videoCaptureDeviceName << "' found." << endl;
		}
	}

	if (deviceInfo)
		delete deviceInfo;

	return videoCaptureDeviceName;
}

void WebcamWebrtcPeer::getBestVideoCaptureCapability(int requestedVideoCaptureWidth,
			int requestedVideoCaptureHeight, int requestedVideoCaptureMaxFps) {
	if(bestVideoCaptureCapability)
		return;

	webrtc::VideoCaptureModule::DeviceInfo *deviceInfo = webrtc::VideoCaptureFactory::CreateDeviceInfo();
	if(deviceInfo && deviceInfo->NumberOfDevices() > 0) {
		char videoCaptureDeviceName[256];
		char videoCaptureDeviceId[256];
		if (deviceInfo->GetDeviceName(0, videoCaptureDeviceName, 256, videoCaptureDeviceId, 256) == 0) {
			cout << "Video capture named '" << videoCaptureDeviceName << "' found." << endl;
			cout << "It's all capabilities: " << endl;

			int capabilities = deviceInfo->NumberOfCapabilities(videoCaptureDeviceId);
			webrtc::VideoCaptureCapability videoCaptureCapability;
			for (int j = 0; j < capabilities; j++) {
				if(deviceInfo->GetCapability(videoCaptureDeviceId,j,videoCaptureCapability) == 0) {
					cout << "Video Type: " << getVideoTypeString(videoCaptureCapability.videoType) <<
						", Width: " << videoCaptureCapability.width <<
						", Height: " << videoCaptureCapability.height <<
						", Max FPS: " << videoCaptureCapability.maxFPS <<
						"." << endl;
				}
			}

			webrtc::VideoCaptureCapability requestedCaptureCapability;
			requestedCaptureCapability.width = requestedVideoCaptureWidth;
			requestedCaptureCapability.height = requestedVideoCaptureHeight;
			requestedCaptureCapability.maxFPS = requestedVideoCaptureMaxFps;

			bestVideoCaptureCapability = new webrtc::VideoCaptureCapability();
			if(deviceInfo->GetBestMatchedCapability(videoCaptureDeviceId,
						requestedCaptureCapability, *bestVideoCaptureCapability) == -1) {
				bestVideoCaptureCapability->width = requestedCaptureCapability.width;
				bestVideoCaptureCapability->height = requestedCaptureCapability.height;
				bestVideoCaptureCapability->maxFPS = requestedCaptureCapability.maxFPS;
				cout << "Can't get best capability of the device. We use requested capability(width, height, maxFPS): " <<
						bestVideoCaptureCapability->width << ", " <<
						bestVideoCaptureCapability->height << ", " <<
						bestVideoCaptureCapability->maxFPS << "." << endl;
			} else {
				cout << "It's best capability(width, height, maxFPS): " <<
						bestVideoCaptureCapability->width << ", " <<
						bestVideoCaptureCapability->height << ", " <<
						bestVideoCaptureCapability->maxFPS << "." << endl;
			}
		}
	}

	if(deviceInfo)
		delete deviceInfo;
}

string WebcamWebrtcPeer::getVideoTypeString(webrtc::VideoType videoType) {
	if (videoType == webrtc::VideoType::kUnknown) {
		return "kUnknown";
	} else if (videoType == webrtc::VideoType::kI420) {
		return "kI420";
	} else if(videoType == webrtc::VideoType::kIYUV) {
		return "kIYUV";
	} else if(videoType == webrtc::VideoType::kRGB24) {
		return "kRGB24";
	} else if(videoType == webrtc::VideoType::kABGR) {
		return "kABGR";
	} else if(videoType == webrtc::VideoType::kARGB) {
		return "kARGB";
	} else if(videoType == webrtc::VideoType::kARGB4444) {
		return "kARGB4444";
	} else if(videoType == webrtc::VideoType::kRGB565) {
		return "kRGB565";
	} else if(videoType == webrtc::VideoType::kARGB1555) {
		return "kARGB1555";
	} else if(videoType == webrtc::VideoType::kYUY2) {
		return "kYUY2";
	} else if(videoType == webrtc::VideoType::kYV12) {
		return "kYV12";
	} else if(videoType == webrtc::VideoType::kUYVY) {
		return "kUYVY";
	} else if(videoType == webrtc::VideoType::kMJPEG) {
		return "kMJPEG";
	} else if(videoType == webrtc::VideoType::kNV21) {
		return "kNV21";
	} else if(videoType == webrtc::VideoType::kNV12) {
		return "kNV12";
	} else if(videoType == webrtc::VideoType::kBGRA) {
		return "kBGRA";
	} else {
		return "kUnknown";
	}
}

WebcamWebrtcPeer::~WebcamWebrtcPeer() {
	if (videoCaptureDeviceName) {
		delete videoCaptureDeviceName;
		videoCaptureDeviceName = nullptr;
	}

	if (bestVideoCaptureCapability) {
		delete bestVideoCaptureCapability;
		bestVideoCaptureCapability = nullptr;
	}
}

void WebcamWebrtcPeer::OnSignalingChange(webrtc::PeerConnectionInterface::SignalingState newState) {
	cout << "Signaling changed. new Signaling state: " << newState << "." << endl;
}

void WebcamWebrtcPeer::OnAddStream(rtc::scoped_refptr<webrtc::MediaStreamInterface> stream) {}
void WebcamWebrtcPeer::OnRemoveStream(rtc::scoped_refptr<webrtc::MediaStreamInterface> stream) {}
void WebcamWebrtcPeer::OnDataChannel(rtc::scoped_refptr<webrtc::DataChannelInterface> dataChannel) {
	cout << "Data channel is ready." << endl;
}

void WebcamWebrtcPeer::OnRenegotiationNeeded() {}
void WebcamWebrtcPeer::OnIceConnectionChange(
		webrtc::PeerConnectionInterface::IceConnectionState new_state) {
	cout << "ICE connection changed. New state: " << new_state << "." << endl;
}

void WebcamWebrtcPeer::OnStandardizedIceConnectionChange(
		webrtc::PeerConnectionInterface::IceConnectionState new_state) {
	cout << "Standardized ICE connection changed. New state: " << new_state << "." << endl;	
}

void WebcamWebrtcPeer::OnConnectionChange(
		webrtc::PeerConnectionInterface::PeerConnectionState new_state) {
	// cout << "Connection changed. New state: " << new_state << "." << endl;
	cout << "Connection changed." << endl;
}

void WebcamWebrtcPeer::OnIceGatheringChange(
		webrtc::PeerConnectionInterface::IceGatheringState new_state) {
	cout << "ICE gathering changed. New state: " << new_state << endl;
}

void WebcamWebrtcPeer::OnIceCandidate(const webrtc::IceCandidateInterface *candidate) {
	std::string iceCandidateFound = "ICE_CANDIDATE_FOUND " +
		createJsonCandidate(candidate);
	handle->Write(iceCandidateFound.c_str(), iceCandidateFound.size());
}

std::string WebcamWebrtcPeer::createJsonCandidate(const webrtc::IceCandidateInterface *candidate) {
	Json::Value vCandidate;

	vCandidate[nameCandidateSdpMid] = candidate->sdp_mid();
	vCandidate[nameCandidateSdpMLineIndex] = candidate->sdp_mline_index();

	std::string sCandidate;
	if(!candidate->ToString(&sCandidate)) {
		cout << "Error. Failed to serialize candidate." << endl;
	}
	vCandidate[nameCandidateSdp] = sCandidate;

	Json::StyledWriter writer;
	return writer.write(vCandidate);
}

void WebcamWebrtcPeer::OnIceCandidateError(const std::string &host_candidate,
		const std::string &url, int error_code, const std::string &error_text) {
	cout << "ICE candidate error occurred. Error text: " << error_text << endl;
}

void WebcamWebrtcPeer::OnIceCandidateError(const std::string &address, int port,
		const std::string &url, int error_code, const std::string &error_text) {
	cout << "ICE candidate error occurred. Error text: " << error_text << endl;
}

void WebcamWebrtcPeer::OnIceCandidatesRemoved(const std::vector<cricket::Candidate>& candidates) {}
void WebcamWebrtcPeer::OnIceConnectionReceivingChange(bool receiving) {}
void WebcamWebrtcPeer::OnIceSelectedCandidatePairChanged(cricket::CandidatePairChangeEvent &event) {
	cout << "ICE selected candidate pair changed. Local address of new candidate pair: " <<
		event.selected_candidate_pair.local_candidate().address().ToString() <<
		". Local address of new candidate pair: " <<
		event.selected_candidate_pair.remote_candidate().address().ToString() << "." << endl;
}
void WebcamWebrtcPeer::OnAddTrack(rtc::scoped_refptr<webrtc::RtpReceiverInterface> receiver,
		const std::vector<rtc::scoped_refptr<webrtc::MediaStreamInterface>> &streams) {
	cout << "Peer track added. Size of streams: " << streams.size() << "." << endl;
}
void WebcamWebrtcPeer::OnTrack(rtc::scoped_refptr<webrtc::RtpTransceiverInterface> transceiver) {
	cout << "Track added. Media type: " << transceiver->media_type() << "." << endl;
}
void WebcamWebrtcPeer::OnRemoveTrack(rtc::scoped_refptr<webrtc::RtpReceiverInterface> receiver) {}
void WebcamWebrtcPeer::OnInterestingUsage(int usage_pattern) {}
