package com.suprogramuota_visata.vedlys.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.ChatMessage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.suprogramuota_visata.vedlys.AppSettings

class NotificationsViewModel(private val apiClient: ApiSvClient) : BaseViewModel() {

    private val chatRepo = apiClient.chatRepository

    var chatItems by mutableStateOf<List<ChatMessage>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set
    var isSending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var chatUsers by mutableStateOf<List<Map<String, String>>>(emptyList())
        private set
        
    var showReadMessages by mutableStateOf(false)
        private set
        
    private var allMessages = emptyList<ChatMessage>()
    private var readIds = emptySet<String>()

    init {
        load()
    }

    fun toggleShowReadMessages(show: Boolean) {
        showReadMessages = show
        updateChatItems()
    }

    private fun updateChatItems() {
        chatItems = allMessages.filter { showReadMessages || !readIds.contains(it.id) }.sortedByDescending { it.timestamp }
    }

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                chatRepo.connect()
                val usersResult = chatRepo.getChatUsers()
                if (usersResult is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
                    chatUsers = usersResult.data
                }
                
                launch {
                    AppSettings.readMessageIds.collect { ids ->
                        readIds = ids
                        updateChatItems()
                    }
                }
                
                chatRepo.messages.onEach { messages ->
                    allMessages = messages
                    updateChatItems()
                    isLoading = false
                }.launchIn(scope)
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Nepavyko prisijungti prie pokalbių: ${e.message}"
            }
        }
    }

    fun send(messageText: String, priority: String, receiverId: String? = null) {
        scope.launch {
            isSending = true
            try {
                // In a real app, senderId would come from auth or config. Using a default for now.
                val msg = ChatMessage(
                    senderId = "VedlysUser",
                    receiverId = receiverId,
                    message = messageText,
                    priority = com.suprogramuota_visata.api.domain.models.MessagePriority.valueOf(priority)
                )
                chatRepo.sendMessage(msg)
                isSending = false
            } catch (e: Exception) {
                isSending = false
                errorMessage = "Nepavyko išsiųsti: ${e.message}"
            }
        }
    }

    fun dismiss(id: String) {
        val msg = allMessages.find { it.id == id }
        if (msg != null) {
            val relatedIds = allMessages.filter {
                it.timestamp <= msg.timestamp &&
                (it.senderId == msg.senderId || it.receiverId == msg.senderId ||
                 it.senderId == msg.receiverId || it.receiverId == msg.receiverId)
            }.map { it.id }
            AppSettings.addReadMessageIds(relatedIds + id)
        } else {
            AppSettings.addReadMessageId(id)
        }
    }

    fun clearError() { errorMessage = null }

    override fun dispose() {
        super.dispose()
        scope.launch {
            chatRepo.disconnect()
        }
    }
}
