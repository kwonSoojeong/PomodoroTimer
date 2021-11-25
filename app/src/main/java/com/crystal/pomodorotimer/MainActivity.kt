package com.crystal.pomodorotimer

import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private val remainMinutesTextView: TextView by lazy {
        findViewById(R.id.remainMinutesTextView)
    }
    private val remainSecondsTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }
    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekbar)
    }
    private var currentCountDownTimer: CountDownTimer? = null

    private val soundPool: SoundPool = SoundPool.Builder().build()
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        initSounds()
    }

    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    //Seekbar progress = 직접 값을 줘서 조작할 때도 해당 함수 호출된다.
                    if(fromUser)
                        updateRemainTime(progress * 60 * 100L)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    //새로운 타이머를 셋업하고 싶어서 조작할 경우, 기존 카운트다운을 cancel
                    currentCountDownTimer?.cancel()
                    currentCountDownTimer = null
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return //엘비스 오퍼레이터 null 일경우 오른쪽을 리턴한다,  , return 값이아니라 익스프레션 리턴
                    //트레킹 터치가 끝났을 때. 타이머를 시작한다
                    startCountDown(seekBar)

                }
            }
        )
    }

    private fun startCountDown(seekBar: SeekBar) {
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)
        currentCountDownTimer?.start()

        tickingSoundId?.let { soundId ->
            //Null이 아닐 경우에만
            soundPool.play(soundId, 1F, 1F, 0, -1, 1F)
        }
    }

    private fun initSounds(){
        tickingSoundId = soundPool.load(this,R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)
    }

    private fun createCountDownTimer(initialMillis: Long) =
        object : CountDownTimer(initialMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                //1초마다 한번씩
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
        }


    private fun completeCountDown() {
        updateRemainTime(0)
        updateSeekBar(0)

        soundPool.autoPause() //Ticking sound
        bellSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)
        }
    }

    private fun updateRemainTime(remainMillis: Long) {
        val remainSeconds = remainMillis / 1000

        remainMinutesTextView.text = "%02d".format(remainSeconds / 60) //분
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60) //초
        Log.d(
            "MainActivity",
            "Text update" + remainMinutesTextView.text + ", " + remainSecondsTextView.text
        )
    }

    private fun updateSeekBar(remainMillis: Long) {
        seekBar.progress = (remainMillis / 1000 / 60).toInt()
    }


//    override fun onStart() {
//        super.onStart()
//        Log.d("onStart","onStart")
//    }
    override fun onResume() {
        super.onResume()
        Log.d("onResume","onResume")
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("onPause","onPause")
        soundPool.autoPause()
    }

//    override fun onStop() {
//        super.onStop()
//        Log.d("OnStop","Onstop")
//    }

    override fun onDestroy() {
        super.onDestroy()
        //메모리 해지
        soundPool.release()
    }
}