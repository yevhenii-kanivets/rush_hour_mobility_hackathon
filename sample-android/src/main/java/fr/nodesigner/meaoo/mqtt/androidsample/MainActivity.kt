package fr.nodesigner.meaoo.mqtt.androidsample

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import fr.nodesigner.meaoo.androidsample.R
import fr.nodesigner.meaoo.mqtt.android.TOPIC
import fr.nodesigner.meaoo.mqtt.android.listener.IMessageCallback
import fr.nodesigner.meaoo.mqtt.androidsample.entity.Mission
import fr.nodesigner.meaoo.mqtt.androidsample.entity.UserSituation
import kotlinx.android.synthetic.main.main_activity.tvPosition
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage

private const val TAG = "Olala"

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        Singleton.setInternalCb(object : IMessageCallback {

            override fun connectionLost(cause: Throwable) {
                var errorMessage = "connectionLost"
                if (cause.message != null) {
                    errorMessage = cause.message!!
                }
                cause.printStackTrace()
                Log.v(TAG, "connectionLost $errorMessage")
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                val jsonString = String(mqttMessage.payload)
                when (topic) {
                    TOPIC.USER_SITUATION.path -> userSituationUpdate(jsonString)
                    TOPIC.USER_MISSION.path, TOPIC.USER_MISSION_DEV.path -> {
                        userMissionUpdate(jsonString)
                    }
                    else -> Log.v(TAG, jsonString)
                }
            }

            override fun deliveryComplete(messageToken: IMqttDeliveryToken) {
                Log.v(TAG, "in delivery complete ${messageToken.message}")
            }

            override fun onConnectionSuccess(token: IMqttToken) {
                Toast.makeText(this@MainActivity, "connnected to server", Toast.LENGTH_SHORT).show()
                Singleton.subscribeToAllTopics()
            }

            override fun onConnectionFailure(token: IMqttToken, throwable: Throwable) {
                var errorMessage = "connection error"
                if (throwable.message != null) {
                    errorMessage = throwable.message!!
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onDisconnectionSuccess(token: IMqttToken) {
                Log.v(TAG, "onDisconnectionSuccess")
            }

            override fun onDisconnectionFailure(token: IMqttToken, throwable: Throwable) {
                Log.v(TAG, "onDisconnectionFailure")
            }
        })
        connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        Singleton.disconnect(false)
    }

    private fun connect() {
        Singleton.setupApplication(applicationContext, false, true)
        Singleton.connect()
    }

    private val gson = Gson()

    private fun userSituationUpdate(jsonString: String) {
        val userSituation = gson.fromJson<UserSituation>(jsonString, UserSituation::class.java)
        tvPosition.text = "x: ${userSituation.position.x}, y: ${userSituation.position.y}"
    }

    private fun userMissionUpdate(jsonString: String) {
        val mission = gson.fromJson<Mission>(jsonString, Mission::class.java)
        Log.d(TAG, mission.toString())
    }
}