package tmf.host.util;

import tmf.host.VersionSet;

@FunctionalInterface
public interface VersionSetObjectNameResolver {
    Object resolve(VersionSet vs, String reference);

    default Object resolveToObject(VersionSet vs, String reference) {
        Object resolved = this.resolve(vs, reference);
        return resolved;
    }
}
