package com.thefirstlineofcode.sand.client.ibtr;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.stream.Features;
import com.thefirstlineofcode.chalk.core.AbstractChatClient;
import com.thefirstlineofcode.chalk.core.stream.AbstractStreamer;
import com.thefirstlineofcode.chalk.core.stream.IStreamNegotiant;
import com.thefirstlineofcode.chalk.core.stream.IStreamer;
import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.chalk.core.stream.StreamConfig;
import com.thefirstlineofcode.chalk.core.stream.negotiants.InitialStreamNegotiant;
import com.thefirstlineofcode.chalk.core.stream.negotiants.tls.IPeerCertificateTruster;
import com.thefirstlineofcode.chalk.core.stream.negotiants.tls.TlsNegotiant;
import com.thefirstlineofcode.chalk.network.IConnection;
import com.thefirstlineofcode.chalk.network.SocketConnection;
import com.thefirstlineofcode.sand.protocols.ibtr.Register;

class IbtrChatClient extends AbstractChatClient {
	
	private IPeerCertificateTruster peerCertificateTruster;

	public IbtrChatClient(StreamConfig streamConfig) {
		this(streamConfig, new SocketConnection());
	}
	
	public IbtrChatClient(StreamConfig streamConfig, IConnection connection) {
		super(streamConfig, connection);
	}
	
	public void setPeerCertificateTruster(IPeerCertificateTruster peerCertificateTruster) {
		this.peerCertificateTruster = peerCertificateTruster;
	}

	public IPeerCertificateTruster getPeerCertificateTruster() {
		return peerCertificateTruster;
	}

	@Override
	protected IStreamer createStreamer(StreamConfig streamConfig, IConnection connection) {
		IbtrStreamer streamer = new IbtrStreamer(getStreamConfig(), connection);
		streamer.setConnectionListener(this);
		streamer.setNegotiationListener(this);
		
		if (peerCertificateTruster != null) {
			streamer.setPeerCertificateTruster(peerCertificateTruster);
		} else {
			// always trust peer certificate
			streamer.setPeerCertificateTruster(new IPeerCertificateTruster() {				
				@Override
				public boolean accept(Certificate[] certificates) {
					return true;
				}
			});
		}
		
		return streamer;
	}

	private class IbtrStreamer extends AbstractStreamer {
		private IPeerCertificateTruster certificateTruster;
		
		public IbtrStreamer(StreamConfig streamConfig, IConnection connection) {
			super(streamConfig, connection);
		}
		
		@Override
		protected List<IStreamNegotiant> createNegotiants() {
			List<IStreamNegotiant> negotiants = new ArrayList<>();
			
			InitialStreamNegotiant initialStreamNegotiant = createIbtrSupportedInitialStreamNegotiant();
			negotiants.add(initialStreamNegotiant);
			
			TlsNegotiant tls = createIbtrSupportedTlsNegotiant();
			negotiants.add(tls);
			
			IbtrNegotiant ibtr = createIbtrNegotiant();
			negotiants.add(ibtr);
			
			setNegotiationReadResponseTimeout(negotiants);
			
			return negotiants;
		}

		private IbtrNegotiant createIbtrNegotiant() {
			return new IbtrNegotiant();
		}
		
		public void setPeerCertificateTruster(IPeerCertificateTruster certificateTruster) {
			this.certificateTruster = certificateTruster;
		}

		private InitialStreamNegotiant createIbtrSupportedInitialStreamNegotiant() {
			return new IbtrSupportedInitialStreamNegotiant(streamConfig.getHost(), streamConfig.getLang());
		}
		
		private TlsNegotiant createIbtrSupportedTlsNegotiant() {
			TlsNegotiant tls = new IbtrSupportedTlsNegotiant(streamConfig.getHost(), streamConfig.getLang(),
					((StandardStreamConfig)streamConfig).isTlsPreferred());
			tls.setPeerCertificateTruster(certificateTruster);
			return tls;
		}
	}
	
	private static class IbtrSupportedInitialStreamNegotiant extends InitialStreamNegotiant {
		
		static {
			oxmFactory.register(ProtocolChain.first(Features.PROTOCOL).next(Register.PROTOCOL),
					new CocParserFactory<>(Register.class));
		}

		public IbtrSupportedInitialStreamNegotiant(String hostName, String lang) {
			super(hostName, lang);
		}
		
	}
	
	private static class IbtrSupportedTlsNegotiant extends TlsNegotiant {
		
		static {
			oxmFactory.register(ProtocolChain.first(Features.PROTOCOL).next(Register.PROTOCOL),
					new CocParserFactory<>(Register.class));
		}

		public IbtrSupportedTlsNegotiant(String hostName, String lang, boolean tlsPreferred) {
			super(hostName, lang, tlsPreferred);
		}
		
	}
	
}
