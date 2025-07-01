package com.example.vidabnb

import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import io.mockk.every
import io.mockk.mockk
import com.example.vidabnb.data.api.VidaBNBApiService
import com.example.vidabnb.data.api.AuthTokenHolder
import com.example.vidabnb.data.repository.AuthRepository

class AuthRepositoryTest {
    private lateinit var mockApiService: VidaBNBApiService
    private lateinit var mockAuthTokenHolder: AuthTokenHolder


    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        mockApiService = mockk()
        mockAuthTokenHolder = mockk(relaxed = false)
        authRepository = AuthRepository(mockApiService, mockAuthTokenHolder)
    }

    @Test
    fun `isAuthenticated returns true when AuthTokenHolder is authenticated`() {
        every { mockAuthTokenHolder.isAuthenticated() } returns true
        val result = authRepository.isAuthenticated()
        assertTrue(result)
    }

    @Test
    fun `isAuthenticated returns false when AuthTokenHolder is not authenticated`() {
        every { mockAuthTokenHolder.isAuthenticated() } returns false
        val result = authRepository.isAuthenticated()
        assertFalse(result)
    }

    @Test
    fun `isAuthenticated returns false after logout`() {
        every { mockAuthTokenHolder.isAuthenticated() } returns true
        every { mockAuthTokenHolder.clearAuthData() } answers {
            every { mockAuthTokenHolder.isAuthenticated() } returns false
        }
        authRepository.logout()
        assertFalse(authRepository.isAuthenticated())
    }
}