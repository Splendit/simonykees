package eu.jsparrow.ui.util;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.UsbDevice;

/**
 * A wrapper for the functionality that we use from OSHI library.
 * 
 * @since 3.0.0
 *
 */
public class OshiUtil {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private OshiUtil() {
		/*
		 * Hiding public constructor
		 */
	}

	private static final String MISSING = "missing-hardware-id"; //$NON-NLS-1$

	public static String createSecretFromHardware() {

		String diskSerial = ""; //$NON-NLS-1$
		SystemInfo systemInfo = new SystemInfo();

		HardwareAbstractionLayer hal = systemInfo.getHardware();
		HWDiskStore[] diskStores = hal.getDiskStores();

		if (diskStores.length > 0) {
			diskSerial = diskStores[0].getSerial();
		}

		return diskSerial;
	}

	public static String createNameFromHardware() {
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
	private static List<String> findAllUsbSerialNumbers() {
		SystemInfo systemInfo = new SystemInfo();
		HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
		UsbDevice[] usbDevices = hardwareAbstractionLayer.getUsbDevices(false);
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
	public static List<String> findAllDiskSerialNumbers() {

		SystemInfo systemInfo = new SystemInfo();
		HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
		HWDiskStore[] diskStores = hardwareAbstractionLayer.getDiskStores();

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
	 * Finds the first serial number of the disk which is not a USB device. Uses
	 * {@link #findAllDiskSerialNumbers()} to retrieve the serial numbers of all
	 * disks. Uses {@link #findAllUsbSerialNumbers()}. Drops all USB serial
	 * numbers from the disk serial numbers. Sorts the result and returns the
	 * first nonempty value. This is done because a USB device could be
	 * recognized by the system as a disk.
	 * 
	 * @return the first nonempty disk number which is not a USB device.
	 */
	public String findFirstDiskNumber() {
		List<String> usbDevices = findAllUsbSerialNumbers();
		List<String> diskSerialNumbers = findAllDiskSerialNumbers();

		List<String> nonUsbDiskSerials = new ArrayList<>();
		nonUsbDiskSerials.addAll(diskSerialNumbers);

		nonUsbDiskSerials.removeAll(usbDevices);

		Collections.sort(nonUsbDiskSerials);

		if (nonUsbDiskSerials.isEmpty()) {
			Collections.sort(diskSerialNumbers);
			return diskSerialNumbers.stream()
				.filter(s -> !s.isEmpty())
				.findFirst()
				.orElse(MISSING);
		}

		return nonUsbDiskSerials.get(0);
	}

}
