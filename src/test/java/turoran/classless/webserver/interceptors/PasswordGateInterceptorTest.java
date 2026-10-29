package turoran.classless.webserver.interceptors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGateInterceptorTest {

    @Test
    void roguelikeInfoPageIsPublic() {
        assertTrue(PasswordGateInterceptor.isPublicInfoPage("/roguelike"));
        assertTrue(PasswordGateInterceptor.isPublicInfoPage("/roguelike/"));
        assertTrue(PasswordGateInterceptor.isPublicInfoPage("/roguelike/catalog.json"));
        assertTrue(PasswordGateInterceptor.isPublicInfoPage("/roguelike/icons/ability_kick.jpg"));
    }

    @Test
    void everythingElseStaysGated() {
        assertFalse(PasswordGateInterceptor.isPublicInfoPage("/roguelikeX"));
        assertFalse(PasswordGateInterceptor.isPublicInfoPage("/files/downloadlauncher/windows"));
        assertFalse(PasswordGateInterceptor.isPublicInfoPage("/components/main.html"));
        assertFalse(PasswordGateInterceptor.isPublicInfoPage("/roguelike/../files/download/windows"));
    }
}
