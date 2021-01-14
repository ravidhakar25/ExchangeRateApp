package com.exchangerate.repository


class NetworkRepository {
    companion object {
        private var networkRepository: NetworkRepository? = null
        fun instance(): NetworkRepository {
            synchronized(this) {
                if (networkRepository == null)
                    networkRepository = NetworkRepository()
                return networkRepository!!
            }
        }
    }

    init {
        if (networkRepository != null) {
            throw Exception("Use Single instance")
        }
    }


}