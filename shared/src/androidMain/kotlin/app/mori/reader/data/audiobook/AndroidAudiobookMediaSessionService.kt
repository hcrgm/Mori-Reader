package app.mori.reader.data.audiobook

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import org.koin.core.context.GlobalContext

class AndroidAudiobookMediaSessionService : MediaSessionService() {
    private val playerRepository: AndroidAudiobookPlayerRepository
        get() =
            GlobalContext
                .get()
                .get<AudiobookPlayerRepository>() as AndroidAudiobookPlayerRepository

    override fun onCreate() {
        super.onCreate()
        addSession(playerRepository.mediaSession())
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = playerRepository.mediaSession()
}
