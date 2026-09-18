package tss.web.Bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;


@Named
@RequestScoped
public class LoginErrorBean implements Serializable {
    private static final long serialVersionUID=1L;
    
    private boolean error;
    
    public boolean isError() {
        return error;
    }
    
    public void setError(boolean error)
    {
    
    this.error=error;
    }
    
}
