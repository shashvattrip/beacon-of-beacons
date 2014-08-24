/*
 * The MIT License
 *
 * Copyright 2014 Miroslav Cupak (mirocupak@gmail.com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dnastack.beacon.service;

import com.dnastack.beacon.core.Beacon;
import com.dnastack.beacon.core.Query;
import com.dnastack.beacon.util.HttpUtils;
import com.dnastack.beacon.util.ParsingUtils;
import com.dnastack.beacon.util.QueryUtils;
import java.net.MalformedURLException;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.http.client.methods.HttpGet;

/**
 * Kaviar beacon service.
 *
 * @author Miroslav Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
@Stateless
@LocalBean
@Kaviar
public class KaviarBeaconService extends GenomeAwareBeaconService {

    private static final long serialVersionUID = 30L;
    private static final String BASE_URL = "http://db.systemsbiology.net/kaviar/cgi-pub/beacon";
    private static final String PARAM_TEMPLATE = "?onebased=0&frz=%s&chr=%s&pos=%d&allele=%s";
    // requeries genome specification in the query
    private static final String[] SUPPORTED_REFS = {"hg19", "hg18"};

    @Inject
    private HttpUtils httpUtils;

    @Inject
    private QueryUtils queryUtils;

    @Inject
    private ParsingUtils parsingUtils;

    private String getQueryUrl(String ref, String chrom, Long pos, String allele) throws MalformedURLException {
        String params = String.format(PARAM_TEMPLATE, ref, chrom, pos, allele);

        return BASE_URL + params;
    }

    @Override
    @Asynchronous
    public Future<String> getQueryResponse(Beacon beacon, Query query, String ref) {
        String res = null;

        // should be POST, but the server accepts GET as well
        HttpGet httpGet;
        try {
            httpGet = new HttpGet(getQueryUrl(ref, query.getChromosome(), query.getPosition(), query.getAllele()
            ));
            res = httpUtils.executeRequest(httpGet);
        } catch (MalformedURLException ex) {
            // ignore, already null
        }

        return new AsyncResult<>(res);
    }

    @Override
    @Asynchronous
    public Future<Boolean> parseQueryResponse(String response) {
        Boolean res = parsingUtils.parseYesNoCaseInsensitive(response);

        return new AsyncResult<>(res);
    }

    @Override
    protected String[] getRefs() {
        return SUPPORTED_REFS;
    }
}