package unidue.ub.batch.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import unidue.ub.media.analysis.Nrequests;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.ItemGroup;

import java.net.URL;

import static unidue.ub.media.tools.MonographTools.getNrequestsFor;

public class NrequestsProcessor implements ItemProcessor<Manifestation, Nrequests> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public NrequestsProcessor() {
    }

    @Override
    public Nrequests process(final Manifestation manifestation) throws Exception {
        log.info("transforming manifestation with id " + manifestation.getTitleID());
        ObjectMapper mapper = new ObjectMapper();
        String lendable = mapper.readValue(new URL("http://localhost:8082/api/settings/itemGroup/lendable"), ItemGroup.class).getItemCategoriesAsString();
        return getNrequestsFor(manifestation, lendable);
    }
}
