GRADLEW := ./gradlew

.PHONY: unit integrate build

unit:
	$(GRADLEW) :app:testDebugUnitTest

integrate:
	$(GRADLEW) :app:connectedDebugAndroidTest

build:
	$(GRADLEW) :app:assembleDebug
	@echo "APK: app/build/outputs/apk/debug/app-debug.apk"
