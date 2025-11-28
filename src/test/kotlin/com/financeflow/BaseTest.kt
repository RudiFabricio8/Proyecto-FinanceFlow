package com.financeflow

import io.ktor.server.testing.*
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.mock.MockProvider
import org.koin.test.mock.declareMock
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(KoinTestExtension::class)
abstract class BaseTest : KoinTest {
    protected val testDispatcher = StandardTestDispatcher()
    
    @BeforeEach
    fun setup() {
        // Set up Koin test environment
        stopKoin()
        startKoin {
            modules(emptyList())
        }
        
        // Set up Mockito as the Koin mock provider
        MockProvider.register { kClass ->
            Mockito.mock(kClass.java)
        }
        
        // Set up test dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterEach
    fun tearDown() {
        // Clean up Koin
        stopKoin()
        
        // Clean up mockk
        unmockkAll()
        
        // Reset dispatcher
        Dispatchers.resetMain()
    }
    
    protected fun <T> withTestApplication(test: TestApplicationEngine.() -> T): T {
        return withTestApplication(
            moduleFunction = {},
            test = test
        )
    }
    
    protected fun <T> runTest(block: suspend () -> T) = runTest(testDispatcher) {
        block()
    }
    
    protected inline fun <reified T : Any> mock(noinline stubs: (T) -> Unit = {}): T {
        val mock = MockProvider.mock(T::class)
        stubs(mock as T)
        return mock
    }
    
    protected inline fun <reified T : Any> get(): T = GlobalContext.get().get()
    
    protected inline fun <reified T : Any> declare(noinline instance: () -> T) {
        declareMock<T> {
            instance()
        }
    }
}
