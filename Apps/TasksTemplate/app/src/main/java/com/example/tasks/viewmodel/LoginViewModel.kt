package com.example.tasks.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tasks.service.model.HeaderModel
import com.example.tasks.service.constants.TaskConstants
import com.example.tasks.service.helper.FingerPrintHelper
import com.example.tasks.service.listener.APIListener
import com.example.tasks.service.listener.ValidationListener
import com.example.tasks.service.model.PriorityModel
import com.example.tasks.service.repository.PersonRepository
import com.example.tasks.service.repository.PriorityRepository
import com.example.tasks.service.repository.local.SecurityPreferences
import com.example.tasks.service.repository.remote.RetrofitClient

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val mPersonRepository = PersonRepository(application)
    private val mPriorityRepository = PriorityRepository(application)
    private val mSharedPreferences = SecurityPreferences(application)

    private val mLogin = MutableLiveData<ValidationListener>()
    var login: LiveData<ValidationListener> = mLogin

    private val mFingerPrint = MutableLiveData<Boolean>()
    var fingerPrint: LiveData<Boolean> = mFingerPrint

    fun doLogin(email: String, password: String) {
        mPersonRepository.login(email, password, object: APIListener<HeaderModel> {
            override fun onSuccess(model: HeaderModel) {

                mSharedPreferences.store(TaskConstants.SHARED.TOKEN_KEY, model.token)
                mSharedPreferences.store(TaskConstants.SHARED.PERSON_KEY, model.personKey)
                mSharedPreferences.store(TaskConstants.SHARED.PERSON_NAME, model.name)

                RetrofitClient.addHeader(model.token, model.personKey)

                mLogin.value = ValidationListener()
            }

            override fun onFailure(str: String) {
                mLogin.value = ValidationListener(str)
            }
        })
    }

    fun isAuthenticationAvailable() {
        val token = mSharedPreferences.get(TaskConstants.SHARED.TOKEN_KEY)
        val person = mSharedPreferences.get(TaskConstants.SHARED.PERSON_KEY)
        val everLogged = (token != "" && person != "")

        RetrofitClient.addHeader(token, person)

        if (!everLogged) {
            mPriorityRepository.all(object : APIListener<List<PriorityModel>> {
                override fun onSuccess(result: List<PriorityModel>) {
                    TODO("Not yet implemented")
                }

                override fun onFailure(str: String) {
                    TODO("Not yet implemented")
                }
            })
        }

        if (FingerPrintHelper.isAuthenticationAvailable(getApplication())) {
            mFingerPrint.value = everLogged
        }
    }
}