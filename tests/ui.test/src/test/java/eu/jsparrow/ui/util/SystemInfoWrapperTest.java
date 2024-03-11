package eu.jsparrow.ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.UsbDevice;

public class SystemInfoWrapperTest {
	
	private SystemInfoWrapper systemInfo;
	private HardwareAbstractionLayer hardwareAbstractionLayer;
	private HWDiskStore diskStore;
	private CentralProcessor centralProcessor;
	private UsbDevice usbDevice;
	
	
	@BeforeEach
	public void setUp() {
		hardwareAbstractionLayer = mock(HardwareAbstractionLayer.class);
		diskStore = mock(HWDiskStore.class);
		centralProcessor = mock(CentralProcessor.class);
		usbDevice = mock(UsbDevice.class);
		
		when(hardwareAbstractionLayer.getUsbDevices(false)).thenReturn(Collections.singletonList(usbDevice));
		when(hardwareAbstractionLayer.getDiskStores()).thenReturn(Collections.singletonList(diskStore));
		when(hardwareAbstractionLayer.getProcessor()).thenReturn(centralProcessor);		
		
		systemInfo = new SystemInfoWrapper(hardwareAbstractionLayer);
	}
	
	@Test
	public void createUniqueHardwareId_missingDiskSerial_shouldReturnProcessorId() {
		String expectedHardwareId = "processor-id";
		ProcessorIdentifier processorIdentifier = new ProcessorIdentifier("Intel", "I7", "family", "model", "stepping", expectedHardwareId, true);
		when(diskStore.getSerial()).thenReturn("");
		when(centralProcessor.getProcessorIdentifier()).thenReturn(processorIdentifier);
		
		String hardwareId = systemInfo.createUniqueHardwareId();
		
		assertEquals(expectedHardwareId, hardwareId);
	}
	
	@Test
	public void createUniqueHardwareId_validDiskSerialNumber_shouldReturnDiskSerialNumber() {
		String expectedHardwareId = "disk-serial-number";
		when(diskStore.getSerial()).thenReturn(expectedHardwareId);
		
		String hardwareId = systemInfo.createUniqueHardwareId();
		
		assertEquals(expectedHardwareId, hardwareId);
	}
	
	@Test
	public void createUniqueHardwareId_skipUsbSerialNumber_shouldReturnProcessorId() {
		String expectedHardwareId = "processor-id";
		String usbId = "usb-id";
		ProcessorIdentifier processorIdentifier = new ProcessorIdentifier("Intel", "I7", "family", "model", "stepping", expectedHardwareId, true);
		when(diskStore.getSerial()).thenReturn(usbId);
		when(usbDevice.getSerialNumber()).thenReturn(usbId);
		when(centralProcessor.getProcessorIdentifier()).thenReturn(processorIdentifier);
		
		String hardwareId = systemInfo.createUniqueHardwareId();
		
		assertEquals(expectedHardwareId, hardwareId);
	}
	
	@Test
	public void createUniqueHardwareId_missingHardwareInfo_shouldReturnDefaultValue() {
		when(diskStore.getSerial()).thenReturn("");
		when(usbDevice.getSerialNumber()).thenReturn("");
//		when(centralProcessor.getProcessorID()).thenReturn("");
		
		String hardwareId = systemInfo.createUniqueHardwareId();
		
		assertEquals("missing-hardware-id", hardwareId);
	}

}
