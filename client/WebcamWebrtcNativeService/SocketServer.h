#ifndef SOCKET_SERVER_H
#define SOCKET_SERVER_H

#include "cppnet.h"

#include "WebcamWebrtcPeer.h"

class SocketServer {
public:
	SocketServer(rtc::scoped_refptr<WebcamWebrtcPeer> wwPeer);

	void start();
	void stop();

	void messageRead(cppnet::Handle handle, std::shared_ptr<cppnet::Buffer> data, uint32_t len);
	void connected(cppnet::Handle handle, uint32_t err);
	void disconnected(cppnet::Handle handle, uint32_t err);

	~SocketServer();
private:
	void processMessage(cppnet::Handle handle, const std::string &message);
	void processCommand(cppnet::Handle handle,const std::string &command);
	bool getRequestedWebcamCapabilityArgs(std::string args, int *requestedVideoCaptureWidth,
		int *requestedVideoCaptureHeight, int *requestedVideoCaptureMaxFps);
private:
	cppnet::CppNet *net;
	rtc::scoped_refptr<WebcamWebrtcPeer> wwPeer;
	bool started;
	bool alreadyConnected;
};

#endif