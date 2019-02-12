package eu.jsparrow.ui.util;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.UsbDevice;

@SuppressWarnings("nls")
public class SystemInfoWrapperTest {
	
	private SystemInfoWrapper systemInfo;
	private HardwareAbstractionLayer hardwareAbstractionLayer;
	private HWDiskStore diskStore;
	private CentralProcessor centralProcessor;
	private UsbDevice usbDevice;
	
	
	@Before
	public void setUp() {
		hardwareAbstractionLayer = mock(HardwareAbstractionLayer.class);
		diskStore = mock(HWDiskStore.class);
		centralProcessor = mock(CentralProcessor.class);
		usbDevice = mock(UsbDevice.class);
		
		when(hardwareAbstractionLayer.getUsbDevices(false)).thenReturn(new UsbDevice [] {usbDevice});
		when(hardwareAbstractionLayer.getDiskStores()).thenReturn(new HWDiskStore [] {diskStore});
		when(hardwareAbstractionLayer.getProcessor()).thenReturn(centralProcessor);		
		
		systemInfo = new SystemInfoWrapper(hardwareAbstractionLayer);
	}
	
	@Test
	public void createUniqueHardwareId_missingDiskSerial_shouldReturnProcessorId() {
		String expectedHardwareId = "processor-id";
		when(diskStore.getSerial()).thenReturn("");
		when(centralProcessor.getProcessorID()).thenReturn(expectedHardwareId);
		
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
		when(diskStore.getSerial()).thenReturn(usbId);
		when(usbDevice.getSerialNumber()).thenReturn(usbId);
		when(centralProcessor.getProcessorID()).thenReturn(expectedHardwareId);
		
		String hardwareId = systemInfo.createUniqueHardwareId();
		
		assertEquals(expectedHardwareId, hardwareId);
	}
	
	@Test
	public void createUniqueHardwareId_missingHardwareInfo_shouldReturnDefaultValue() {
		when(diskStore.getSerial()).thenReturn("");
		when(usbDevice.getSerialNumber()).thenReturn("");
		when(centralProcessor.getProcessorID()).thenReturn("");
		
		String hardwareId = systemInfo.createUniqueHardwareId();
		
		assertEquals("missing-hardware-id", hardwareId);
	}

}
