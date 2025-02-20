
package ch.usi.si.seart.server.dto.user;

import ch.usi.si.seart.validation.constraints.OWASPEmail;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {

    @NotNull
    @OWASPEmail
    String email;
}