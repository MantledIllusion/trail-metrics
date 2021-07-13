package net.javacrumbs.shedlock.provider.jdbctemplate.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = DbSchedulerLock.TABLE_NAME)
public class DbSchedulerLock {

    public static final String TABLE_NAME = "trail_cleanup";

    @Id
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "lock_until", nullable = false)
    private Date lockUntil;

    @Column(name = "locked_at", nullable = false)
    private Date lockedAt;

    @Column(name = "locked_by", length = 255, nullable = false)
    private String lockedBy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(Date lockUntil) {
        this.lockUntil = lockUntil;
    }

    public Date getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Date lockedAt) {
        this.lockedAt = lockedAt;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }
}
