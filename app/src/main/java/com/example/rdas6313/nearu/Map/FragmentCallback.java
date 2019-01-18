package com.example.rdas6313.nearu.Map;

public interface FragmentCallback {
    public void onChatBtnCliked(String currentUserid,String chatUserid,String chatUserName);
    public void onLoadRecentChatThreads();
    public void onLoadSettings();
}
