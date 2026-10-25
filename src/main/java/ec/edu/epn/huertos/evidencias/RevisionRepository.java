package ec.edu.epn.huertos.evidencias;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionRepository extends JpaRepository<Revision, Long> {

    Optional<Revision> findByEnvioId(Long envioId);

    List<Revision> findByEnvioIdIn(Collection<Long> envioIds);

    List<Revision> findByRevisorIdOrderByRevisadaEnDesc(Long revisorId);
}
