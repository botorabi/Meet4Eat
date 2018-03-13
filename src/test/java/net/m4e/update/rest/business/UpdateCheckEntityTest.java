package net.m4e.update.rest.business;

import net.m4e.common.UpdateCheckEntityCreator;
import net.m4e.tests.EntityAssertions;
import net.m4e.update.business.UpdateCheckEntity;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;

import javax.json.bind.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author boto
 * Date of creation March 13, 2018
 */
public class UpdateCheckEntityTest {

    @Test
    void commonEntityTests() {
        EntityAssertions.assertThat(UpdateCheckEntity.class)
                .isSerializable()
                .hasSerialVersionUID()
                .hasEntityAnnotation()
                .hasIdAnnotation()
                .conformsToEqualsContract()
                .hasHashCode()
                .hasProperToString();
    }

    @Test
    void setterGetter() {
        UpdateCheckEntity entity = UpdateCheckEntityCreator.create();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(entity.getId()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_ID);
        softly.assertThat(entity.getName()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_NAME);
        softly.assertThat(entity.getOs()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_OS);
        softly.assertThat(entity.getFlavor()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_FLAVOR);
        softly.assertThat(entity.getVersion()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_VERSION);
        softly.assertThat(entity.getReleaseDate()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_RELEASE_DATE);
        softly.assertThat(entity.getUrl()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_URL);
        softly.assertThat(entity.isActive()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_ACTIVE);
        softly.assertAll();
    }

    @Nested
    class Serialization {

        private Jsonb jsonb;

        @BeforeEach
        void setup() {
            jsonb = JsonbBuilder.create();
        }

        @Test
        void serialize() {
            UpdateCheckEntity entity = UpdateCheckEntityCreator.create();

            String jsonString = jsonb.toJson(entity);

            assertThat(jsonString).contains("id");
            assertThat(jsonString).contains("name");
            assertThat(jsonString).contains("os");
            assertThat(jsonString).contains("flavor");
            assertThat(jsonString).contains("version");
            assertThat(jsonString).contains("url");
            assertThat(jsonString).contains("releaseDate");
            assertThat(jsonString).contains("active");
        }

        @Test
        void deserialize() {
            UpdateCheckEntity input = UpdateCheckEntityCreator.create();

            String jsonString = jsonb.toJson(input);

            UpdateCheckEntity entity = jsonb.fromJson(jsonString, UpdateCheckEntity.class);

            assertThat(entity.getId()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_ID);
            assertThat(entity.getName()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_NAME);
            assertThat(entity.getOs()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_OS);
            assertThat(entity.getFlavor()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_FLAVOR);
            assertThat(entity.getVersion()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_VERSION);
            assertThat(entity.getReleaseDate()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_RELEASE_DATE);
            assertThat(entity.isActive()).isEqualTo(UpdateCheckEntityCreator.CHECK_ENTRY_ACTIVE);
        }
    }
}
