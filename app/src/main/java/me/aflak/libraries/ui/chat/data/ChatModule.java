package me.aflak.libraries.ui.chat.data;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.aflak.bluetooth.Bluetooth;
import me.aflak.libraries.ui.chat.interactor.ChatInteractor;
import me.aflak.libraries.ui.chat.interactor.ChatInteractorImpl;
import me.aflak.libraries.ui.chat.presenter.ChatPresenter;
import me.aflak.libraries.ui.chat.presenter.ChatPresenterImpl;
import me.aflak.libraries.ui.chat.view.ChatView;

/**
 * Created by Omar on 20/12/2017.
 */

@Module
public class ChatModule {
    private ChatView view;

    public ChatModule(ChatView view) {
        this.view = view;
    }

    @Provides @Singleton
    public ChatView provideChatView(){
        return view;
    }

    @Provides @Singleton
    public ChatInteractor provideChatInteractor(Bluetooth bluetooth){
        return new ChatInteractorImpl(bluetooth);
    }

    @Provides @Singleton
    public ChatPresenter provideChatPresenter(ChatView view, ChatInteractor interactor){
        return new ChatPresenterImpl(view, interactor);
    }
}
