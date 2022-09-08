package geb.tmf.util

import groovy.test.GroovyAssert
import org.junit.Test
import tmf.host.util.LinkHelper


class LinkHelperTest extends GroovyAssert {

    @Test
    void testLinkifyText() {
        String res

        res = LinkHelper.linkifyText("Cooperation (APEC) Privacy Principles\nhttp://publications.pec.rg/publication-detail.hp.pub_id=390?")
        assertEquals(
                "Cooperation (APEC) Privacy Principles <a target='_blank' href='http://publications.pec.rg/publication-detail.hp.pub_id=390?'>http://publications.pec.rg/publication-detail.hp.pub_id=390?</a>",
                res)

        res = LinkHelper.linkifyText("Cooperation (APEC) Privacy Principles\nhttps://publications.pec.rg/publication-detail.hp.pub_id=390?abc")
        assertEquals(
                "Cooperation (APEC) Privacy Principles <a target='_blank' href='https://publications.pec.rg/publication-detail.hp.pub_id=390?abc'>https://publications.pec.rg/publication-detail.hp.pub_id=390?abc</a>",
                res)

        res = LinkHelper.linkifyText("\n\r\bAsia-Pacific Economic Cooperation (APEC) Privacy Principles\n" +
                "http://publications.pec.rg/publication-detail.hp.pub_id=390?xyz=1")
        assertEquals( "   Asia-Pacific Economic Cooperation (APEC) Privacy Principles <a target='_blank' href='http://publications.pec.rg/publication-detail.hp.pub_id=390?xyz=1'>http://publications.pec.rg/publication-detail.hp.pub_id=390?xyz=1</a>",
                res)

        res = LinkHelper.linkifyText("\n\r\bAsia-Pacific Economic Cooperation (APEC) Privacy Principles\n" +
                "ftp://ftp.funet.fi/pub/standards/RFC/rfc959.txt")
        assertEquals( "   Asia-Pacific Economic Cooperation (APEC) Privacy Principles <a target='_blank' href='ftp://ftp.funet.fi/pub/standards/RFC/rfc959.txt'>ftp://ftp.funet.fi/pub/standards/RFC/rfc959.txt</a>",
                res)

    }

}