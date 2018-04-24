package unidue.ub.batch.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.ItemProcessor;
import unidue.ub.media.analysis.Nrequests;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.ItemGroup;

import java.net.URL;

import static unidue.ub.media.tools.MonographTools.getNrequestsFor;

public class NrequestsProcessor implements ItemProcessor<Manifestation, Nrequests> {

    public NrequestsProcessor() {
    }

    @Override
    public Nrequests process(final Manifestation manifestation) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String lendable = mapper.readValue(new URL("http://localhost:8082/api/settings/itemGroup/lendable"), ItemGroup.class).getItemCategoriesAsString();
        return getNrequestsFor(manifestation, lendable);
    }
}
