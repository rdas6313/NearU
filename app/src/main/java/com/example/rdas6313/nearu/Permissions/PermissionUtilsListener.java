package com.example.rdas6313.nearu.Permissions;

public interface PermissionUtilsListener {
    public void onExplanation();
    public void onPermissionResult(boolean isGranted);
    public void onDontAskAgain();
}
