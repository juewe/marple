package com.github.flaxsearch.resources;

import com.github.flaxsearch.api.UninvertData;
import com.github.flaxsearch.util.ReaderManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Path("/uninvert")
@Produces(MediaType.APPLICATION_JSON)
public class UninvertResource {

    private final ReaderManager readerManager;

    public UninvertResource(ReaderManager readerManager) {
        this.readerManager = readerManager;
    }

    @GET
    public List<UninvertData> doUninvert(@QueryParam("segment") Integer segment,
                                         @QueryParam("field") String field,
                                         @QueryParam("docidPrefix") String docidPrefix,
                                         @QueryParam("encoding") @DefaultValue("utf8") String encoding) throws IOException {
        // TODO: return real data
        return Arrays.asList(
            new UninvertData(1, "hello world!")
        );
    }
}
