package com.microsoft.intellij.helpers.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.Nullable;
import com.microsoft.tooling.msservices.helpers.tasks.CancellableTask.CancellableTaskHandle;

public class CancellableTaskHandleImpl implements CancellableTaskHandle {
    private ProgressIndicator progressIndicator;
    private Throwable exception;

    @Override
    public boolean isFinished() {
        return !progressIndicator.isRunning();
    }

    @Override
    public boolean isCancelled() {
        return progressIndicator.isCanceled();
    }

    @Override
    public boolean isSuccessful() {
        return isFinished() && !isCancelled() && exception == null;
    }

    @Nullable
    @Override
    public Throwable getException() {
        return exception;
    }

    public void setException(@NotNull Throwable exception) {
        this.exception = exception;
    }

    @Override
    public void cancel() {
        progressIndicator.cancel();
    }

    public void setProgressIndicator(@NotNull ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }
}