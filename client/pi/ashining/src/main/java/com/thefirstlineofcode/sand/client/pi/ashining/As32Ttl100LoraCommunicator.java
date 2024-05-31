package com.thefirstlineofcode.sand.client.pi.ashining;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputProvider;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputProvider;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialProvider;
import com.pi4j.io.serial.StopBits;
import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.sand.client.thing.commuication.AbstractCommunicator;
import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class As32Ttl100LoraCommunicator extends AbstractCommunicator<LoraAddress, LoraAddress, byte[]> {
	private static final Logger logger = LoggerFactory.getLogger(As32Ttl100LoraCommunicator.class);
	
	private static final LoraAddress DEFAULT_COMMUNICATOR_ADDRESS = new LoraAddress(new byte[] {0x00, 0x00, 0x17});
	
	private static final String PROVIDER_PIGPIO_DIGITAL_INPUT = "pigpio-digital-input";
	private static final String PROVIDER_PIGPIO_DIGITAL_OUTPUT = "pigpio-digital-output";
	private static final String PROVIDER_PIGPIO_SERIAL = "pigpio-serial";
	
	private static final int DEFAULT_MD0_PIN = 2;
	private static final int DEFAULT_MD1_PIN = 3;
	private static final int DEFAULT_AUX_PIN = 4;
	
	private static final String DEFAULT_SERIAL_ID = "uart";
	private static final String DEFAULT_SERIAL_NAME = "Serial UART";
	private static final String DEFAULT_DEVICE = "/dev/serial0";
	
	private int md0Pin;
	private int md1Pin;
	private int auxPin;
	
	private DigitalInput auxInput;
	private DigitalOutput md0Output;
	private DigitalOutput md1Output;
	
	private Serial serial;
	
	private byte[] configs;
	private LoraAddress address;
	
	private boolean listening;
	private Thread dataListeningThread;
	
	private String serialId;
	private String serialName;
	private String device;
	
	public As32Ttl100LoraCommunicator() {
		super();
		
		md0Pin = DEFAULT_MD0_PIN;
		md1Pin = DEFAULT_MD1_PIN;
		auxPin = DEFAULT_AUX_PIN;
		
		serialId = DEFAULT_SERIAL_ID;
		serialName = DEFAULT_SERIAL_NAME;
		device = DEFAULT_DEVICE;
		
		listening = false;
	}
	
	public int getMd0Pin() {
		return md0Pin;
	}
	
	public void setMd0Pin(int md0Pin) {
		this.md0Pin = md0Pin;
	}
	
	public int getMd1Pin() {
		return md1Pin;
	}
	
	public void setMd1Pin(int md1Pin) {
		this.md1Pin = md1Pin;
	}
	
	public int getAuxPin() {
		return auxPin;
	}
	
	public void setAuxPin(int auxPin) {
		this.auxPin = auxPin;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		if (!DEFAULT_COMMUNICATOR_ADDRESS.equals(address))
			try {
				changeAddress(DEFAULT_COMMUNICATOR_ADDRESS, true);
			} catch (CommunicationException e) {
				throw new RuntimeException("Can't set to default communicator address.", e);
			}
	}
	
	@Override
	protected void doInitialize() {
		try {
			Context context = Pi4J.newAutoContext();
			createAuxInput(context);
			createMd0Output(context);
			createMd1Output(context);
			createSerial(context);
			
			configs = readLoraChipConfigs();
			
			if (logger.isInfoEnabled())
				logger.info("LoRa chip has intialized. Current LoRa chip configs: {}.", BinaryUtils.getHexStringFromBytes(configs));
			
			address = new LoraAddress(configs[0], configs[1], configs[3]);
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Can't initialize LoRa chip.", e);
			
			throw new RuntimeException("Can't initialize LoRa chip.", e);
		} finally {
			if (md1Output != null)
				md1Output.off();
			
			if (md0Output != null)
				md0Output.off();
		}
	}
	
	@Override
	protected void doConfigure() {
		try {
			if ((configs[4] & 0x80) != 0x80) {
				if (logger.isInfoEnabled())
					logger.info("LoRa chip isn't in P2P transmission mode. Configure it to P2P transmission mode.");
				
				configureP2pTransmissionMode();
				configs = readLoraChipConfigs();
			}
			
			if (logger.isInfoEnabled())
				logger.info("LoRa chip has configured. Current LoRa chip Configs: {}.",
						BinaryUtils.getHexStringFromBytes(configs));
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Can't configure LoRa chip.", e);
			
			throw new RuntimeException("Can't configure LoRa chip.", e);
		} finally {
			if (md1Output != null)
				md1Output.off();
			
			if (md0Output != null)
				md0Output.off();
		}
	}
	
	protected byte[] executeConfigurationCommand(byte[] configurationCommand) {
		boolean oldListening = isListening();
		try {
			if (listening)
				stopToListen();
			
			while (auxInput.state().isLow()) {
				if (logger.isDebugEnabled())
					logger.debug("Waiting Lora Chip to be idle to do configuration....");
				
				Thread.sleep(500);
			}
			
			md0Output.on();
			md1Output.on();
			
			Thread.sleep(1000);
			
			while (serial.available() > 0) {
				// Clear all received data in serial.
				byte[] buffer = new byte[256];
				serial.read(buffer);
				Thread.sleep(500);
			}
			
			serial.write(configurationCommand);
			
			Thread.sleep(1000);
			
			return readDataFromSerial();
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Can't initialize LoRa chip.", e);
			
			throw new RuntimeException("Can't configure LoRa chip.", e);
		} finally {
			if (md1Output != null)
				md1Output.off();
			
			if (md0Output != null)
				md0Output.off();
			
			if (oldListening)
				startToListen();
		}
	}

	private byte[] readDataFromSerial() {
		int avaliable = serial.available();
		if (avaliable <= 0)
			return null;
		
		byte[] buffer = new byte[256];
		int size = serial.read(buffer);
		if (size < 0)
			throw new RuntimeException(String.format("%d bytes received.", size));
		
		if (size == 0)
			return null;
		
		return Arrays.copyOfRange(buffer, 0, size);
	}
	
	private void configureP2pTransmissionMode() throws InterruptedException {
		byte[] setToP2pTransmissionModeCommand = {(byte)0xc0, configs[0], configs[1],
			configs[2], configs[3], (byte)(configs[4] | 0x80)};
		
		byte[] response = executeConfigurationCommand(setToP2pTransmissionModeCommand);
		if (response == null || !isOk(response))
			throw new RuntimeException(String.format("Failed to set P2P transmission mode. Resonse: %s.",
					response == null ? "null" : BinaryUtils.getHexStringFromBytes(response)));
	}
	
	private boolean isOk(byte[] response) {
		return response.length >= 2 && ((response[0] & 0xff) == 0x4f && (response[1] & 0xff) == 0x4b);
	}

	public byte[] readLoraChipConfigs() {
		byte[] readConfigsCommand = {(byte)0xc1, (byte)0xc1, (byte)0xc1};
		
		byte[] response = executeConfigurationCommand(readConfigsCommand);
		if (response == null)
			throw new RuntimeException(String.format("No response returned for reading default configs command."));
		
		if (response.length != 6)
			throw new RuntimeException(String.format("Wrong response returned for reading default configs command. Response: %s.",
					BinaryUtils.getHexStringFromBytes(response)));
		
		return Arrays.copyOfRange(response, 1, response.length);
	}
	
	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}

	public String getSerialName() {
		return serialName;
	}

	public void setSerialName(String serialName) {
		this.serialName = serialName;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	private void createSerial(Context context) {
		SerialConfig serialConfig = Serial.newConfigBuilder(context).
				id(serialId).
				name(serialName).
				device(device).
				baud(Baud._9600).
				dataBits(DataBits._8).
				parity(Parity.NONE).
				stopBits(StopBits._1).
				build();
			SerialProvider serialProvider = context.provider(PROVIDER_PIGPIO_SERIAL);
			serial = serialProvider.create(serialConfig);
			serial.open();
	}

	private void createMd0Output(Context context) {
		DigitalOutputConfig md0OutputConfig = DigitalOutput.newConfigBuilder(context).
				address(md0Pin).
				build();
		
		DigitalOutputProvider digitalOutputProvider = context.provider(PROVIDER_PIGPIO_DIGITAL_OUTPUT);
		md0Output = digitalOutputProvider.create(md0OutputConfig);
	}
	
	private void createMd1Output(Context context) {
		DigitalOutputConfig md1OutputConfig = DigitalOutput.newConfigBuilder(context).
				address(md1Pin).
				build();
		
		DigitalOutputProvider digitalOutputProvider = context.provider(PROVIDER_PIGPIO_DIGITAL_OUTPUT);
		md1Output = digitalOutputProvider.create(md1OutputConfig);
	}
	
	private void createAuxInput(Context context) {
		DigitalInputProvider digitalInputProvider = context.provider(PROVIDER_PIGPIO_DIGITAL_INPUT);
		
		DigitalInputConfig auxInputConfig = DigitalInput.newConfigBuilder(context).
			address(auxPin).
			build();
		auxInput = digitalInputProvider.create(auxInputConfig);
	}

	@Override
	public LoraAddress getAddress() {
		return address;
	}

	@Override
	protected synchronized void doStartToListen() {
		if (dataListeningThread == null)
			dataListeningThread = new Thread(new DataReceiver(), String.format("Data Receiver Thread for Lora Communicator(%s).",
				this.getAddress()));
		
		if (!dataListeningThread.isAlive())
			dataListeningThread.start();
	}

	@Override
	public synchronized void stopToListen() {
		listening = false;
		if (dataListeningThread == null)
			return;
		
		try {
			dataListeningThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException("????", e);
		}
		dataListeningThread = null;
	}

	@Override
	public boolean isListening() {
		return listening;
	}
	
	private class DataReceiver implements Runnable {		
		@Override
		public void run() {
			listening = true;
			
			while (listening) {
				receive();
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}
	
	public synchronized void receive() {
		byte[] data = readDataFromSerial();
				
		if (data != null && data.length > 0) {
			if (logger.isDebugEnabled())
				logger.debug("{} bytes data received. Data: {}.", data.length,
						BinaryUtils.getHexStringFromBytes(data));
			
			received(null, data);
		}
	}
	
	@Override
	protected void doChangeAddress(LoraAddress address, boolean savePersistently) throws CommunicationException {
		try {
			byte configurationCommandByte = (byte)0xc2;
			if (savePersistently)
				configurationCommandByte = (byte)0xc0;
			
			byte[] changeAddressCommand = {configurationCommandByte, address.getAddressHighByte(), address.getAddressLowByte(),
					configs[2], address.getChannel(), configs[4]};
			
			byte[] response = executeConfigurationCommand(changeAddressCommand);
			if (response == null || !isOk(response)) {
				if (logger.isErrorEnabled())
					logger.error("Failed to change Lora chip address. Response: {}.",
							response == null ? "null" : BinaryUtils.getHexStringFromBytes(response));
				throw new RuntimeException(String.format("Failed to change Lora chip address. Response: %s.",
						response == null ? "null" : BinaryUtils.getHexStringFromBytes(response)));
			}
			
			this.address = address;
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Failed to change address.", e);
			
			throw new CommunicationException("Failed to change address.", e);
		}
		
		if (logger.isInfoEnabled()) {
			logger.info("Communicator address has changed. Current LoRa chip address: {}.", address);
		}
	}

	@Override
	protected synchronized void doSend(LoraAddress to, byte[] data) throws CommunicationException {
		byte[] finalDataBeSent = new byte[3 + data.length];
		finalDataBeSent[0] = to.getAddressHighByte();
		finalDataBeSent[1] = to.getAddressLowByte();
		finalDataBeSent[2] = to.getChannel();
		
		System.arraycopy(data, 0, finalDataBeSent, 3, data.length);
		serial.write(finalDataBeSent);
		
		if (logger.isDebugEnabled())
				logger.debug("{} bytes has written to peer {}. Data: {}.", finalDataBeSent.length, to,
						BinaryUtils.getHexStringFromBytes(finalDataBeSent));
	}
}
