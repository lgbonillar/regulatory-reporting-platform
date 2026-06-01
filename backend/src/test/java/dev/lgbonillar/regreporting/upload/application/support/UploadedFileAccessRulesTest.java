package dev.lgbonillar.regreporting.upload.application.support;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;
import dev.lgbonillar.regreporting.users.domain.UserStatus;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UploadedFileAccessRulesTest {

    @Test
    void canListAsReturnsTrueForRootUser() {
        User root = rootUser();
        User target = analystUser();

        assertThat(UploadedFileAccessRules.canListAs(root, target)).isTrue();
    }

    @Test
    void canListAsReturnsTrueForAdministratorUser() {
        User admin = administratorUser();
        User target = analystUser();

        assertThat(UploadedFileAccessRules.canListAs(admin, target)).isTrue();
    }

    @Test
    void canListAsReturnsTrueForAnalystListingOwnFiles() {
        User analyst = analystUser();
        User sameAnalyst = sameAnalystUser();

        assertThat(UploadedFileAccessRules.canListAs(analyst, sameAnalyst)).isTrue();
    }

    @Test
    void canListAsReturnsFalseForAnalystListingOtherUserFiles() {
        User analyst = analystUser();
        User otherAnalyst = otherAnalystUser();

        assertThat(UploadedFileAccessRules.canListAs(analyst, otherAnalyst)).isFalse();
    }

    @Test
    void canListAsReturnsFalseForAuditorUser() {
        User auditor = auditorUser();
        User target = analystUser();

        assertThat(UploadedFileAccessRules.canListAs(auditor, target)).isFalse();
    }

    @Test
    void canViewReturnsTrueForRootUser() {
        User root = rootUser();
        UploadedFile file = storedFile(root);

        assertThat(UploadedFileAccessRules.canView(root, file)).isTrue();
    }

    @Test
    void canViewReturnsTrueForAdministratorUser() {
        User admin = administratorUser();
        UploadedFile file = storedFile(admin);

        assertThat(UploadedFileAccessRules.canView(admin, file)).isTrue();
    }

    @Test
    void canViewReturnsTrueForAnalystOwningFile() {
        User analyst = analystUser();
        UploadedFile file = storedFile(analyst);

        assertThat(UploadedFileAccessRules.canView(analyst, file)).isTrue();
    }

    @Test
    void canViewReturnsFalseForAnalystOwningFileButDifferentUsername() {
        User analyst = analystUser();
        User otherAnalyst = otherAnalystUser();
        UploadedFile file = storedFile(otherAnalyst);

        assertThat(UploadedFileAccessRules.canView(analyst, file)).isFalse();
    }

    @Test
    void canViewReturnsFalseForAuditorUser() {
        User auditor = auditorUser();
        UploadedFile file = storedFile(analystUser());

        assertThat(UploadedFileAccessRules.canView(auditor, file)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = UploadedFileStatus.class, names = {"STORED", "PENDING_CORRECTION"})
    void isViewableStatusReturnsTrueForDownloadableStatuses(UploadedFileStatus status) {
        assertThat(UploadedFileAccessRules.isViewableStatus(status)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = UploadedFileStatus.class, names = {"PENDING_VALIDATION", "MISSING", "FAILED", "DELETED"})
    void isViewableStatusReturnsFalseForNonDownloadableStatuses(UploadedFileStatus status) {
        assertThat(UploadedFileAccessRules.isViewableStatus(status)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = UploadedFileStatus.class, names = {"STORED", "PENDING_CORRECTION"})
    void isDownloadableStatusReturnsTrueForDownloadableStatuses(UploadedFileStatus status) {
        assertThat(UploadedFileAccessRules.isDownloadableStatus(status)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = UploadedFileStatus.class, names = {"PENDING_VALIDATION", "MISSING", "FAILED", "DELETED"})
    void isDownloadableStatusReturnsFalseForNonDownloadableStatuses(UploadedFileStatus status) {
        assertThat(UploadedFileAccessRules.isDownloadableStatus(status)).isFalse();
    }

    private User rootUser() {
        User user = mock(User.class);
        when(user.hasRole(UserRole.ROOT)).thenReturn(true);
        when(user.hasRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(user.hasRole(UserRole.ANALYST)).thenReturn(false);
        when(user.hasRole(UserRole.AUDITOR)).thenReturn(false);
        return user;
    }

    private User administratorUser() {
        User user = mock(User.class);
        when(user.hasRole(UserRole.ROOT)).thenReturn(false);
        when(user.hasRole(UserRole.ADMINISTRATOR)).thenReturn(true);
        when(user.hasRole(UserRole.ANALYST)).thenReturn(false);
        when(user.hasRole(UserRole.AUDITOR)).thenReturn(false);
        return user;
    }

    private User analystUser() {
        User user = mock(User.class);
        when(user.hasRole(UserRole.ROOT)).thenReturn(false);
        when(user.hasRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(user.hasRole(UserRole.ANALYST)).thenReturn(true);
        when(user.hasRole(UserRole.AUDITOR)).thenReturn(false);
        when(user.getUsername()).thenReturn("analyst01");
        UUID sharedId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(user.getId()).thenReturn(sharedId);
        return user;
    }

    private User sameAnalystUser() {
        User user = mock(User.class);
        when(user.hasRole(UserRole.ROOT)).thenReturn(false);
        when(user.hasRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(user.hasRole(UserRole.ANALYST)).thenReturn(true);
        when(user.hasRole(UserRole.AUDITOR)).thenReturn(false);
        when(user.getUsername()).thenReturn("analyst01");
        UUID sharedId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(user.getId()).thenReturn(sharedId);
        return user;
    }

    private User otherAnalystUser() {
        User user = mock(User.class);
        when(user.hasRole(UserRole.ROOT)).thenReturn(false);
        when(user.hasRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(user.hasRole(UserRole.ANALYST)).thenReturn(true);
        when(user.hasRole(UserRole.AUDITOR)).thenReturn(false);
        when(user.getUsername()).thenReturn("analyst02");
        when(user.getId()).thenReturn(java.util.UUID.randomUUID());
        return user;
    }

    private User auditorUser() {
        User user = mock(User.class);
        when(user.hasRole(UserRole.ROOT)).thenReturn(false);
        when(user.hasRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(user.hasRole(UserRole.ANALYST)).thenReturn(false);
        when(user.hasRole(UserRole.AUDITOR)).thenReturn(true);
        return user;
    }

    private UploadedFile storedFile(User owner) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/analyst01/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                owner
        );
    }
}