#include <iostream>
#include <thread>

#include "SocketServer.h"

SocketServer::SocketServer(rtc::scoped_refptr<WebcamWebrtcPeer> wwPeer) {
	started = false;
	alreadyConnected = false;
	this->wwPeer = wwPeer;
}

void SocketServer::start() {
	if (started)
		return;
		
	net = new cppnet::CppNet();
	net->Init(2);
	net->ListenAndAccept("127.0.0.1", 9000);

	net->SetAcceptCallback(std::bind(&SocketServer::connected, this, std::placeholders::_1, std::placeholders::_2));
	net->SetReadCallback(std::bind(&SocketServer::messageRead, this, std::placeholders::_1, std::placeholders::_2, std::placeholders::_3));
	net->SetDisconnectionCallback(std::bind(&SocketServer::disconnected, this, std::placeholders::_1, std::placeholders::_2));

	started = true;
	alreadyConnected = false;

	net->Join();
}

void SocketServer::messageRead(cppnet::Handle handle, std::shared_ptr<cppnet::Buffer> data, uint32_t len) {
	const int bufSize = 1024 * 8;

	if (len >= bufSize) {
		cout << "Received a huge message which's size is " << len << ". Ignore to process the message." << endl;
		return;
	}

	char buf[bufSize] = {0};
	int readSize = data->Read(buf, bufSize);

	data->Clear();

	if (readSize > 0) {
		std::string message(buf, readSize);
		cout << "Received client message: " << message << endl;
		processMessage(handle, message);
	} else {
		cout << "Can't read message from data." << endl;
	}
}

void SocketServer::processMessage(cppnet::Handle handle, const std::string &message) {
	std::vector<std::string> commands;

	int commandStart = 0;
	for (int i = 2; i < message.size(); i++) {
		if (message.at(i) == '$' &&
			message.at(i - 1) == '$') {
			commands.push_back(message.substr(commandStart, (i - commandStart - 1)));
			commandStart = i + 1;
		}
	}

	for (int i = 0; i < commands.size(); i++) {
		processCommand(handle, commands[i]);
	}
}

void SocketServer::processCommand(cppnet::Handle handle, const std::string &command) {
	if(wwPeer->isClosed() &&
		command.compare("STOP") != 0 &&
		command.compare("CLOSE") != 0 &&
		command.compare(0, 5, "OPEN ") != 0) {
		std::string error = "ERROR Only STOP, OPEN, CLOSE commands can be processed in closed state.";
		handle->Write(error.c_str(),error.size());

		return;
	}

	if(command.compare("STOP") == 0) {
		cout << "Stop command received. The service will be stopped." << endl;
		stop();

		return;
	} else if(command.compare(0, 5, "OPEN ") == 0) {
		cout << "Open command received." << endl;

		if (wwPeer->isOpened()) {
			cout << "Webcam has opened. Ignore repeated open command." << endl;
			return;
		}
		
		if (command.size() <= 5) {
			std::string error = "Illegal STOP command format.";
			handle->Write(error.c_str(), error.size());

			return;
		}

		int requestedVideoCaptureWidth = -1;
		int requestedVideoCaptureHeight = -1;
		int requestedVideoCaptureMaxFps = -1;
		if (!getRequestedWebcamCapabilityArgs(command.substr(6, command.size()),
					&requestedVideoCaptureWidth, &requestedVideoCaptureHeight,
					&requestedVideoCaptureMaxFps)) {
			
		}
		
		wwPeer->open(requestedVideoCaptureWidth,
			requestedVideoCaptureHeight, requestedVideoCaptureMaxFps);
	} else if(command.compare("CLOSE") == 0) {
		cout << "Close command received." << endl;
		wwPeer->close();
	} else if(command.compare(0, 6, "OFFER ") == 0 && command.size() > 6) {
		std::string offerSdp = command.substr(6, command.size() - 6);
		cout << "Offer command received. SDP is: " + offerSdp << endl;

		wwPeer->offered(offerSdp);
	} else if(command.compare(0, 20, "ICE_CANDIDATE_FOUND ") == 0 && command.size() > 20) {
		std::string jsonCandidate = command.substr(20, command.size() - 20);
		cout << "ICE candidate found command received. Candidate is: " + jsonCandidate << endl;

		wwPeer->iceCandidateFound(jsonCandidate);
	} else {
		cout << "Unkown command received. Message is: " + command << endl;
	}
}

bool SocketServer::getRequestedWebcamCapabilityArgs(std::string args,
		int *requestedVideoCaptureWidth, int *requestedVideoCaptureHeight,
		int *requestedVideoCaptureMaxFps) {
	size_t firstComma = args.find_first_of(',');
	if (firstComma == -1 || firstComma == args.size() - 1)
		return false;

	size_t secondComma = args.find_first_of(',', firstComma + 1);
	if(secondComma == -1 || secondComma == args.size() - 1)
		return false;

	try {
		*requestedVideoCaptureWidth = stoi(args.substr(0, firstComma));
		*requestedVideoCaptureHeight = stoi(args.substr(firstComma + 1, secondComma));
		*requestedVideoCaptureMaxFps = stoi(args.substr(secondComma + 1, args.size()));
	} catch (std::invalid_argument &) {
		return false;
	}

	return true;
}

void SocketServer::connected(cppnet::Handle handle, uint32_t err) {
	static const char conflictMsg[] = "CONFLICT";

	std::string ip;
	uint16_t port;
	if (handle->GetAddress(ip, port)) {
		cout << "A client has connected. Client address: " << ip << ":" << port << "." << endl;
	} else {
		cout << "A client has connected. Failed to get Client addressl" << endl;
	}

	if (alreadyConnected) {
		cout << "Conflict. A client has already connected. The secondary connected client will be closed." << endl;

		handle->Write(conflictMsg, sizeof(conflictMsg));
		handle->Close();

		return;
	}

	wwPeer->setSocketHandle(handle);
	alreadyConnected = true;

	if (err) {
		cout << "Connected error: " + err;
	}
}

void SocketServer::disconnected(cppnet::Handle handle, uint32_t err) {
	cout << "A client has disconnected." << endl;

	if (err) {
		cout << "Disconnected error: " + err;
	}

	wwPeer->setSocketHandle(nullptr);
}

void SocketServer::stop() {
	if (wwPeer.get()) {
		wwPeer->close();
		wwPeer->setSocketHandle(nullptr);
	}

	if (net) {
		net->Destory();
		net = nullptr;
	}
}

SocketServer::~SocketServer() {
	if (net)
		stop();
}