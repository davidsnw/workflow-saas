package com.workflow.workflow_saas.auditing;

import java.time.Instant;

public interface Auditable {
    Instant getCreatedAt();
    Instant getUpdatedAt();
    void setUpdatedAt(Instant updatedAt);
}
