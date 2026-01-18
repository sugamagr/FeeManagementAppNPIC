package com.navoditpublic.fees.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for NumberToWords utility class.
 * Tests conversion of numeric amounts to Indian currency words format.
 */
class NumberToWordsTest {

    @Test
    fun `convert zero amount returns Zero Rupees Only`() {
        val result = NumberToWords.convert(0.0)
        assertThat(result).isEqualTo("Zero Rupees Only")
    }

    @Test
    fun `convert single digit amounts`() {
        assertThat(NumberToWords.convert(1.0)).isEqualTo("One Rupees Only")
        assertThat(NumberToWords.convert(5.0)).isEqualTo("Five Rupees Only")
        assertThat(NumberToWords.convert(9.0)).isEqualTo("Nine Rupees Only")
    }

    @Test
    fun `convert teen numbers`() {
        assertThat(NumberToWords.convert(10.0)).isEqualTo("Ten Rupees Only")
        assertThat(NumberToWords.convert(11.0)).isEqualTo("Eleven Rupees Only")
        assertThat(NumberToWords.convert(15.0)).isEqualTo("Fifteen Rupees Only")
        assertThat(NumberToWords.convert(19.0)).isEqualTo("Nineteen Rupees Only")
    }

    @Test
    fun `convert tens`() {
        assertThat(NumberToWords.convert(20.0)).isEqualTo("Twenty Rupees Only")
        assertThat(NumberToWords.convert(25.0)).isEqualTo("Twenty Five Rupees Only")
        assertThat(NumberToWords.convert(50.0)).isEqualTo("Fifty Rupees Only")
        assertThat(NumberToWords.convert(99.0)).isEqualTo("Ninety Nine Rupees Only")
    }

    @Test
    fun `convert hundreds`() {
        assertThat(NumberToWords.convert(100.0)).isEqualTo("One Hundred Rupees Only")
        assertThat(NumberToWords.convert(101.0)).isEqualTo("One Hundred One Rupees Only")
        assertThat(NumberToWords.convert(150.0)).isEqualTo("One Hundred Fifty Rupees Only")
        assertThat(NumberToWords.convert(999.0)).isEqualTo("Nine Hundred Ninety Nine Rupees Only")
    }

    @Test
    fun `convert thousands`() {
        assertThat(NumberToWords.convert(1000.0)).isEqualTo("One Thousand Rupees Only")
        assertThat(NumberToWords.convert(1500.0)).isEqualTo("One Thousand Five Hundred Rupees Only")
        assertThat(NumberToWords.convert(8300.0)).isEqualTo("Eight Thousand Three Hundred Rupees Only")
        assertThat(NumberToWords.convert(9999.0)).isEqualTo("Nine Thousand Nine Hundred Ninety Nine Rupees Only")
    }

    @Test
    fun `convert ten thousands`() {
        assertThat(NumberToWords.convert(10000.0)).isEqualTo("Ten Thousand Rupees Only")
        assertThat(NumberToWords.convert(12345.0)).isEqualTo("Twelve Thousand Three Hundred Forty Five Rupees Only")
        assertThat(NumberToWords.convert(50000.0)).isEqualTo("Fifty Thousand Rupees Only")
    }

    @Test
    fun `convert lakhs - Indian numbering system`() {
        assertThat(NumberToWords.convert(100000.0)).isEqualTo("One Lakh Rupees Only")
        assertThat(NumberToWords.convert(150000.0)).isEqualTo("One Lakh Fifty Thousand Rupees Only")
        assertThat(NumberToWords.convert(250000.0)).isEqualTo("Two Lakh Fifty Thousand Rupees Only")
        assertThat(NumberToWords.convert(999999.0)).isEqualTo("Nine Lakh Ninety Nine Thousand Nine Hundred Ninety Nine Rupees Only")
    }

    @Test
    fun `convert crores - Indian numbering system`() {
        assertThat(NumberToWords.convert(10000000.0)).isEqualTo("One Crore Rupees Only")
        assertThat(NumberToWords.convert(15000000.0)).isEqualTo("One Crore Fifty Lakh Rupees Only")
        assertThat(NumberToWords.convert(12345678.0)).isEqualTo("One Crore Twenty Three Lakh Forty Five Thousand Six Hundred Seventy Eight Rupees Only")
    }

    @Test
    fun `convert amount with paise`() {
        assertThat(NumberToWords.convert(100.50)).isEqualTo("One Hundred Rupees and Fifty Paise Only")
        assertThat(NumberToWords.convert(0.50)).isEqualTo("Fifty Paise Only")
        assertThat(NumberToWords.convert(1.01)).isEqualTo("One Rupees and One Paise Only")
        assertThat(NumberToWords.convert(99.99)).isEqualTo("Ninety Nine Rupees and Ninety Nine Paise Only")
    }

    @Test
    fun `convert typical school fee amounts`() {
        // Monthly fee
        assertThat(NumberToWords.convert(1200.0)).isEqualTo("One Thousand Two Hundred Rupees Only")
        
        // Annual fee
        assertThat(NumberToWords.convert(14400.0)).isEqualTo("Fourteen Thousand Four Hundred Rupees Only")
        
        // Transport fee
        assertThat(NumberToWords.convert(800.0)).isEqualTo("Eight Hundred Rupees Only")
        
        // Full year tuition
        assertThat(NumberToWords.convert(12000.0)).isEqualTo("Twelve Thousand Rupees Only")
        
        // Large amount with discount
        assertThat(NumberToWords.convert(11000.0)).isEqualTo("Eleven Thousand Rupees Only")
    }

    @Test
    fun `convert complex amounts`() {
        assertThat(NumberToWords.convert(123456.78))
            .isEqualTo("One Lakh Twenty Three Thousand Four Hundred Fifty Six Rupees and Seventy Eight Paise Only")
    }

    @Test
    fun `convert rounds paise correctly`() {
        // Due to floating point, this tests the integer conversion of paise
        val result = NumberToWords.convert(100.5)
        assertThat(result).isEqualTo("One Hundred Rupees and Fifty Paise Only")
    }
}
