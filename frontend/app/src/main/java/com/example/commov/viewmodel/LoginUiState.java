package com.example.commov.viewmodel;

import androidx.annotation.StringRes;

public final class LoginUiState {
    public final String email;
    public final String password;
    public final boolean passwordVisible;
    @StringRes
    public final int emailErrorResId;
    @StringRes
    public final int passwordErrorResId;
    public final boolean loginAccepted;

    public LoginUiState(
            String email,
            String password,
            boolean passwordVisible,
            @StringRes int emailErrorResId,
            @StringRes int passwordErrorResId,
            boolean loginAccepted
    ) {
        this.email = email;
        this.password = password;
        this.passwordVisible = passwordVisible;
        this.emailErrorResId = emailErrorResId;
        this.passwordErrorResId = passwordErrorResId;
        this.loginAccepted = loginAccepted;
    }
}
