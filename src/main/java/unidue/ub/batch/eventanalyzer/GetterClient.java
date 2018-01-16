package unidue.ub.batch.eventanalyzer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import unidue.ub.media.monographs.Manifestation;

import java.util.List;

public interface GetterClient {
    @GetMapping("manifestations/manifestations")
    List<Manifestation> getManifestations(@RequestParam("identifier") String identifier, @RequestParam("exact") String exact,
                                          @RequestParam("mode") String mode);

    @GetMapping("manifestations/fullManifestation")
    Manifestation getFullManifestation(@RequestParam("identifier") String identifier,
                                       @RequestParam("exact") String exact);

    @GetMapping("manifestations/buildFullManifestation")
    Manifestation buildFullManifestation(@RequestParam("identifier") String identifier);

    @GetMapping("manifestations/buildActiveManifestation")
    Manifestation buildActiveManifestation(@RequestParam("identifier") String identifier);
}
