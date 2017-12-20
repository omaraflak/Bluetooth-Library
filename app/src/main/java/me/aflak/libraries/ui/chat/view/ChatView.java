package me.aflak.libraries.ui.chat.view;

/**
 * Created by Omar on 20/12/2017.
 */

public interface ChatView {
    void setStatus(String status);
    void setStatus(int resId);
    void appendMessage(String message);
    void enableHWButton(boolean enabled);
    void showToast(String message);
}
