/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.dto;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author parth
 */
public class AbstractDTO implements Serializable {
    private static final long serialVersionUID = 5231885890948093876L;

    @XmlElement
    private String uuid;
    @XmlElement
    private int jpaVersion;

    protected AbstractDTO() {
    }

    protected AbstractDTO(String uuid, int jpaVersion) {
        this.uuid = uuid;
        this.jpaVersion = jpaVersion;
    }

    public int getJpaVersion() {
        return jpaVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setJpaVersion(int jpaVersion) {
        this.jpaVersion = jpaVersion;
    }

    @XmlTransient
    @JsonbTransient
    public boolean isNew() {
        return uuid == null;
    }

    @Override
    public int hashCode() {
        if (uuid == null) {
            throw new IllegalStateException("uuid not set");
        }
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.uuid);
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
        final AbstractDTO other = (AbstractDTO) obj;
        return Objects.equals(this.uuid, other.uuid);
    }
}
