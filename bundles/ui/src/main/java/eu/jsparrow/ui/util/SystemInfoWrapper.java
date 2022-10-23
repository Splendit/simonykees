package eu.jsparrow.ui.util;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.UsbDevice;

/**
 * A wrapper for the functionality that we use from OSHI library.
 * 
 * @since 3.0.0
 *
 */
public class SystemInfoWrapper {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String MISSING = "missing-hardware-id"; //$NON-NLS-1$

	private HardwareAbstractionLayer hardwareAbstractionLayer;

	public SystemInfoWrapper() {
		SystemInfo systemInfo = new SystemInfo();
		this.hardwareAbstractionLayer = systemInfo.getHardware();
	}

	SystemInfoWrapper(HardwareAbstractionLayer hardwareAbstractionLayer) {
		this.hardwareAbstractionLayer = hardwareAbstractionLayer;
	}

	/**
	 * 
	 * @return a unique identifier for the machine.
	 */
	public String createUniqueHardwareId() {
		String diskSerialNumber = findFirstDiskNumber();
		if (!diskSerialNumber.isEmpty()) {
			return diskSerialNumber;
		}

		String processorId = findProcessorId();
		if (!processorId.isEmpty()) {
			return processorId;
		}

		return MISSING;
	}

	/**
	 * 
	 * @return the host name of the machine
	 */
	public String createNameFromHardware() {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			return addr.getHostName();
		} catch (UnknownHostException e) {
			logger.warn("Error while reading the host name", e); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Find the list of all serial numbers of the USB devices that can be found
	 * in the system. Discards the empty values.
	 * 
	 * @return list of nonempty USB serial numbers connected in the system.
	 */
	private List<String> findAllUsbSerialNumbers() {

		List<UsbDevice> usbDevices = hardwareAbstractionLayer.getUsbDevices(false);
		List<String> usbSerialNumbers = new ArrayList<>();
		for (UsbDevice usbDevice : usbDevices) {
			String deviceSerialNumber = usbDevice.getSerialNumber();
			if (deviceSerialNumber != null && !deviceSerialNumber.isEmpty()) {
				usbSerialNumbers.add(deviceSerialNumber);
			}
		}
		return usbSerialNumbers;
	}

	/**
	 * Finds the serial numbers of the disks that can be found in the system.
	 * Discards empty values.
	 * 
	 * @return List of nonempty disk serial numbers.
	 */
	private List<String> findAllDiskSerialNumbers() {

		List<HWDiskStore> diskStores = hardwareAbstractionLayer.getDiskStores();

		List<String> diskSerialNumbers = new ArrayList<>();

		for (HWDiskStore diskStore : diskStores) {
			String diskSerialNumber = diskStore.getSerial();
			if (diskSerialNumber != null && !diskSerialNumber.isEmpty()) {
				diskSerialNumbers.add(diskSerialNumber);
			}
		}

		return diskSerialNumbers;
	}

	/**
	 * Makes use of {@link CentralProcessor#getProcessorID()} to find an
	 * identifier for the CPU.
	 * 
	 * @return the CPU identifier.
	 */
	private String findProcessorId() {
		CentralProcessor processor = hardwareAbstractionLayer.getProcessor();
		return Optional.ofNullable(processor.getProcessorIdentifier())
				.map(ProcessorIdentifier::getProcessorID)
				.orElse(""); //$NON-NLS-1$
	}

	/**
	 * Finds the first serial number of the disk which is not a USB device. Uses
	 * {@link #findAllDiskSerialNumbers()} to retrieve the serial numbers of all
	 * disks. Uses {@link #findAllUsbSerialNumbers()}. Drops all USB serial
	 * numbers from the disk serial numbers. Sorts the result and returns the
	 * first nonempty value. This is done because a USB device could be
	 * recognized by the system as a disk.
	 * 
	 * @return the first nonempty disk number which is not a USB device or an
	 *         empty string if none is found.
	 */
	private String findFirstDiskNumber() {
		List<String> usbDevices = findAllUsbSerialNumbers();
		List<String> diskSerialNumbers = findAllDiskSerialNumbers();

		List<String> nonUsbDiskSerials = new ArrayList<>();
		nonUsbDiskSerials.addAll(diskSerialNumbers);

		nonUsbDiskSerials.removeAll(usbDevices);

		if (nonUsbDiskSerials.isEmpty()) {
			return ""; //$NON-NLS-1$
		}

		return nonUsbDiskSerials.get(0);
	}
}
