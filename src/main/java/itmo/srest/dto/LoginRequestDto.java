package itmo.srest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Username is mandatory.")
    @Size(min = 3, max = 20, message = "Username length must be between 3 and 20 characters long.")
    @Pattern(regexp = "^[0-9a-zA-Z-_]+", message = "Username should contain only latin characters a/A-z/Z, numbers 0-9, dash \"-\" and underscore \"_\".")
    private String username;

    @NotBlank(message = "Password is mandatory.")
    @Size(min = 8, max = 72, message = "Password must be at least 8 characters long (max 72).")
    @Pattern(regexp = "^(?! )[\\S\\s]*(?<! )$", message = "No leading or trailing spaces allowed.")
    private String password;
}
