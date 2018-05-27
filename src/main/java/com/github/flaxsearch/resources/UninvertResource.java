package com.github.flaxsearch.resources;

import com.github.flaxsearch.api.UninvertData;
import com.github.flaxsearch.util.ReaderManager;
import jdk.internal.joptsimple.internal.Strings;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Path("/uninvert")
@Produces(MediaType.APPLICATION_JSON)
public class UninvertResource {

    private final ReaderManager readerManager;

    public UninvertResource(ReaderManager readerManager) {
        this.readerManager = readerManager;
    }

    static class Position implements Comparable<Position> {
        public final String term;
        public final int pos;

        Position(String term, int pos) {
            this.term = term;
            this.pos = pos;
        }

        @Override
        public int compareTo(Position other) { return pos - other.pos; }
    }

    @GET
    public List<UninvertData> doUninvert(@QueryParam("segment") Integer segment,
                                         @QueryParam("field") String field,
                                         @QueryParam("docidPrefix") String docidPrefix,
                                         @QueryParam("encoding") @DefaultValue("utf8") String encoding) throws IOException {
        if (docidPrefix == null || docidPrefix.isEmpty()) {
            return new ArrayList<>();
        }
        Terms terms = readerManager.terms(segment, field);

        if (terms == null)
            throw new WebApplicationException("No such field " + field, Response.Status.NOT_FOUND);

        List<Position> positions = new ArrayList<>();
        Integer docid = Integer.parseInt(docidPrefix);
        TermsEnum te = terms.iterator();
        BytesRef term;
        PostingsEnum pe = null;
        while ((term = te.next()) != null) {
            int postingFlag = PostingsEnum.FREQS | PostingsEnum.POSITIONS;
            pe = te.postings(pe, postingFlag);
            if (pe.advance(docid) == docid) {
                int freq = pe.freq();
                for (int i = 0; i < freq; ++i) {
                    positions.add(new Position(term.utf8ToString(), pe.nextPosition()));
                }
            }
        }

        Collections.sort(positions);

        StringBuilder sb = new StringBuilder();
        int lastPosition = -1;

        for (Position position : positions) {
            if (position.pos == lastPosition) {
                // term at the same position, use "|" to merge with other terms at the position
                sb.append('|');
            } else if (lastPosition != -1) {
                for (int i = 1; i < position.pos - lastPosition; ++i) {
                    sb.append('!');
                }
                sb.append(' ');
            }
            sb.append(position.term);
            lastPosition = position.pos;
        }

        return Arrays.asList(
            new UninvertData(docid, sb.toString())
        );
    }
}
