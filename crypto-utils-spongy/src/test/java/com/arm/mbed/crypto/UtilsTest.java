package com.arm.mbed.crypto;

import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UtilsTest {

	@Test
	public void convertBigIntegerToByteArrayValidValueSuccessTest() {
		byte[] byteArr = EccUtils.convertBigIntegerToByteArray(new BigInteger(8, 2, new Random()), 5);
		Assert.assertEquals(byteArr.length, 5);
	}

	@Test(expectedExceptions = AssertionError.class)
	public void convertBigIntegerToByteArrayInvalidSizeExceptionTest() {
		EccUtils.convertBigIntegerToByteArray(new BigInteger(8, 2, new Random()), 0);
	}

	@Test(expectedExceptions = AssertionError.class)
	public void convertBigIntegerToByteArrayNullIntegerExceptionTest() {
		EccUtils.convertBigIntegerToByteArray(null, 4);
	}

	@Test
	public void convertBigIntegerToByteSameSizeSuccessTest() {
		byte[] byteArr = EccUtils.convertBigIntegerToByteArray(new BigInteger(32, 2, new Random()), 5);
		Assert.assertEquals(byteArr.length, 5);
	}

	@Test(expectedExceptions = InvalidParameterException.class)
	public void convertBigIntegerToByteArrayIntegerSizeBiggerThanExpectedSizeExceptionTest() {
		EccUtils.convertBigIntegerToByteArray(new BigInteger(34, 2, new Random()), 2);
	}
}
