package tmf.host.util;

import tmf.host.VersionSet;

import java.net.URI;
import java.net.URISyntaxException;

@FunctionalInterface
public interface VersionSetReferenceNameResolver {
    String resolve(VersionSet vs, String reference);
    default URI resolveToURI(VersionSet vs, String reference) throws URISyntaxException {
        String resolved = this.resolve(vs, reference);
        return resolved == null ? null : new URI(resolved);
    }
}
