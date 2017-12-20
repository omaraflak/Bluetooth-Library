package me.aflak.libraries.ui.chat.data;

import javax.inject.Singleton;

import dagger.Component;
import me.aflak.libraries.data.BluetoothModule;
import me.aflak.libraries.ui.chat.view.ChatActivity;

/**
 * Created by Omar on 20/12/2017.
 */
@Singleton
@Component(modules = {BluetoothModule.class, ChatModule.class})
public interface ChatComponent {
    void inject(ChatActivity chatActivity);
}
