
package tss.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@MappedSuperclass
public class AbstractEntity implements Serializable {

    private static final long serialVersionUID = -8910205909777292022L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, length = 36)
    private String uuid;

    @Version
    private int jpaVersion;

    public AbstractEntity() {
        this(false);
    }

    public AbstractEntity(boolean newEntity) {
        if (newEntity) {
            uuid = UUID.randomUUID().toString();
        }
    }

    @PrePersist
    private void ensureUuidIsSet() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    public Long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public int getJpaVersion() {
        return jpaVersion;
    }

    public void setJpaVersion(int jpaVersion) {
        this.jpaVersion = jpaVersion;
    }

    @Override
    public int hashCode() {
        if (uuid == null) {
            throw new IllegalStateException("uuid not set");
        }
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.uuid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (uuid == null) {
            throw new IllegalStateException("uuid not set");
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractEntity other = (AbstractEntity) obj;
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + getId() + " (" + getUuid() + ")";
    }
}
