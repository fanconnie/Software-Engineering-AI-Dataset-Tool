package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.repository.support.ReadOnlyRepository;
import ch.usi.si.seart.views.language.LanguageExtension;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.validation.constraints.NotBlank;
import java.u