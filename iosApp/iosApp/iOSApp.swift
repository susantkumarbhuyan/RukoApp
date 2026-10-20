import SwiftUI
import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging
import FirebaseAppCheck
import ComposeApp

final class RukoAppCheckFactory: NSObject, AppCheckProviderFactory {
    func createProvider(with app: FirebaseApp) -> AppCheckProvider? {
        return AppAttestProvider(app: app)
    }
}

final class RukoAppDelegate: NSObject, UIApplicationDelegate,
    UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(_ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        #if DEBUG
        AppCheck.setAppCheckProviderFactory(AppCheckDebugProviderFactory())
        #else
        AppCheck.setAppCheckProviderFactory(RukoAppCheckFactory())
        #endif
        if FirebaseApp.app() == nil { FirebaseApp.configure() }
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        // Permission is requested by the existing onboarding screen. Registration itself
        // does not show a permission prompt and is safe to repeat.
        application.registerForRemoteNotifications()
        return true
    }

    func application(_ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }

    func application(_ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error) {
        // Simulator/provisioning failures must not prevent normal queue use.
        NSLog("RUKO: APNs registration unavailable")
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken else { return }
        PushNotifications.shared.tokenChanged(token: token)
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .list, .sound])
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void) {
        let info = response.notification.request.content.userInfo
        if let queueId = info["queueId"] as? String {
            DispatchQueue.main.async {
                PushNotifications.shared.openQueue(queueId: queueId, kind: info["kind"] as? String ?? "")
            }
        }
        completionHandler()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(RukoAppDelegate.self) var appDelegate
    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
