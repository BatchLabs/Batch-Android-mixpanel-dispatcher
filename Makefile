all: aar

aar: clean
	./gradlew assembleRelease --no-build-cache && \
	mkdir -p release/ && \
	cp mixpanel-dispatcher/build/outputs/aar/mixpanel-dispatcher-release.aar release/ && \
	cp LICENSE release/

clean:
	./gradlew clean
	rm -rf release/

test:
	./gradlew testDebugUnitTest

test-coverage:
	./gradlew testDebugCoverageUnitTest && \
    awk -F"," '{ instructions += $$4 + $$5; covered += $$5 } END { print covered, "/", instructions, "instructions covered"; print "Total", 100*covered/instructions "% covered" }' mixpanel-dispatcher/build/test-results/jacoco.csv

lint:
	./gradlew lintDebug

ci: clean lint test-coverage aar

publish: aar
	./gradlew mixpanel-dispatcher:publish

.PHONY: ci