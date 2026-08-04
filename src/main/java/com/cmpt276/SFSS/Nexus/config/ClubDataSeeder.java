package com.cmpt276.SFSS.Nexus.config;

import com.cmpt276.SFSS.Nexus.model.Club;
import com.cmpt276.SFSS.Nexus.repository.ClubRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the clubs table with real SFSS club data on every startup.
 *
 * Checks each club by name before inserting it — if a club with that exact
 * name already exists (whether from a previous run of this seeder, or added
 * manually by someone through the admin panel, e.g. test clubs), it's left
 * completely untouched. Only clubs that are genuinely missing get added.
 * This makes it safe to restart/redeploy repeatedly without ever creating
 * duplicates or wiping out manual entries.
 *
 * Source: go.sfss.ca/clubs/list (public SFSS club directory). There's no
 * public JSON API for this data, so it was pulled from the rendered page
 * once and bundled as /resources/data/clubs-seed.json (name, description,
 * logo, and a link back to the official SFSS club profile).
 *
 * The source page doesn't expose category per-club without crawling all 12
 * category filter pages individually, so categories are assigned here via
 * keyword matching against SFSS's real category list (see CATEGORIES below).
 * This is a best-effort classification, not authoritative — if a club is in
 * the wrong bucket, just fix it via the admin panel; it won't be
 * re-seeded once the table has data.
 */
@Component
public class ClubDataSeeder implements CommandLineRunner {

    public static final List<String> CATEGORIES = List.of(
            "Arts", "Business Related", "Career Development", "Charitable",
            "Cultural", "Political", "Social", "Social Justice/Activism",
            "Special Interest/Hobby", "Spiritual", "Technology", "Other"
    );

    // Ordered so more specific categories are checked before catch-alls like Social.
    private static final Map<String, String[]> KEYWORDS = new LinkedHashMap<>();
    static {
        KEYWORDS.put("Spiritual", new String[]{
                "christ", "jesus", "bible", "catholic", "muslim", "islam", "hindu", "sikh",
                "jewish", "hillel", "baha'i", "spiritual", "gospel", "faith", "church", "ismaili",
                "quran", "prophet"});
        KEYWORDS.put("Political", new String[]{
                "political", "democratic party", "ndp", "conservatives", "liberals",
                "electoral", "model united nations"});
        KEYWORDS.put("Social Justice/Activism", new String[]{
                "climate", "human rights", "refugee", "advocacy", "activism", "socialist",
                "awareness", "social action"});
        KEYWORDS.put("Charitable", new String[]{
                "cancer society", "liver foundation", "heart and stroke", "unicef",
                "non-profit", "nonprofit", "fundraising", "donation", "operation smile",
                "blood, organ", "volunteer"});
        KEYWORDS.put("Technology", new String[]{
                "developer", "coding", "programming", "robotics", "cybersecurity", "tech",
                "satellite", "open source", "game develop", "mechanical keyboards",
                "competitive programming", "software", "computer"});
        KEYWORDS.put("Business Related", new String[]{
                "business", "finance", "investment", "accounting", "marketing", "case competition",
                "management information systems", "real estate", "financial literacy", "human resources"});
        KEYWORDS.put("Career Development", new String[]{
                "pre-law", "pre-med", "pre-dental", "pre-vet", "career", "leadership",
                "networking", "professional develop"});
        KEYWORDS.put("Arts", new String[]{
                "art", "photograph", "choir", "orchestra", "jazz", "music", "dance",
                "creative writing", "comics", "graphic novel", "circus"});
        KEYWORDS.put("Cultural", new String[]{
                "chinese", "filipino", "indian ", "vietnamese", "korean", "japanese", "iranian",
                "arab", "afghan", "bangladesh", "pakistan", "thai", "taiwanese", "hong kong",
                "ukrainian", "turkic", "kurd", "ethiopian", "eritrean", "tamil", "malaysia",
                "singapore", "latin american", "latino", "brazilian", "hanfu", "punjabi",
                "hindu culture", "international club", "cultural", "heritage", "cross-cultural"});
        KEYWORDS.put("Special Interest/Hobby", new String[]{
                "chess", "anime", "hiking", "backpacking", "ski and snowboard", "smash",
                "magic the gathering", "pokemon", "tabletop", "knitting", "esports", "golf",
                "cycling", "dragon boat", "calisthenics", "outdoors", "astronomy",
                "sports analytics", "swifties", "polyglot", "tea"});
        KEYWORDS.put("Social", new String[]{
                "social club", "socializing", "meet new people", "friendship", "connect students",
                "welcoming community", "inclusive community", "welcoming and inclusive",
                "sense of community", "get together"});
    }

    private final ClubRepository clubRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClubDataSeeder(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try (InputStream in = new ClassPathResource("data/clubs-seed.json").getInputStream()) {
            List<Map<String, String>> raw = objectMapper.readValue(in, List.class);
            int saved = 0;
            int skipped = 0;
            for (Map<String, String> entry : raw) {
                String name = entry.get("name");
                if (clubRepository.existsByNameIgnoreCase(name)) {
                    skipped++; // already there (e.g. seeded before, or someone added it manually) — leave it alone
                    continue;
                }
                Club club = new Club();
                club.setName(name);
                club.setDescription(entry.get("description"));
                club.setLogoUrl(entry.get("logoUrl"));
                club.setImageUrl(entry.get("logoUrl"));
                club.setWebsite(entry.get("website"));
                club.setCategory(categorize(entry.get("name"), entry.get("description")));
                club.setActive(true);
                club.setMemberCount(0);
                clubRepository.save(club);
                saved++;
            }
            System.out.println("[ClubDataSeeder] Added " + saved + " new clubs, skipped " + skipped
                    + " already present (existing clubs, including any manually-added ones, were left untouched)");
        }
    }

    private String categorize(String name, String description) {
        String haystack = ((name == null ? "" : name) + " " + (description == null ? "" : description))
                .toLowerCase();
        for (Map.Entry<String, String[]> entry : KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                if (haystack.contains(kw)) {
                    return entry.getKey();
                }
            }
        }
        return "Other";
    }
}
