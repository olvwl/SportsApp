package com.example.core.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.core.domain.model.Sport
import com.example.core.domain.repository.ISportRepository
import com.example.core.source.local.remote.network.ApiResponse
import com.example.core.source.local.remote.response.SportResponse
import com.example.core.utils.AppExecutors
import com.example.core.utils.DataMapper
import kotlinx.coroutines.flow.map
import java.util.concurrent.Flow

class SportRepository (
    private val remoteDataSource: com.example.core.source.local.RemoteDataSource,
    private val localDataSource: com.example.core.source.LocalDataSource,
    private val appExecutors: AppExecutors
) : ISportRepository{

    override fun getAllSport(): kotlinx.coroutines.flow.Flow<Resource<List<Sport>>> =
        object : com.example.core.source.local.NetworkBoundResource<List<Sport>, List<SportResponse>>() {
            override fun loadFromDB(): kotlinx.coroutines.flow.Flow<List<Sport>> {
                return localDataSource.getAllSport().map {
                    DataMapper.mapEntitiesToDomain(it)
                }
            }

            override fun shouldFetch(data: List<Sport>?): Boolean =
               data == null || data.isEmpty()

            override suspend fun createCall(): kotlinx.coroutines.flow.Flow<ApiResponse<List<SportResponse>>> =
                remoteDataSource.getAllSport()

            override suspend fun saveCallResult(data: List<SportResponse>) {
                val sportList = DataMapper.mapResponsesToEntities(data)
                localDataSource.insertSport(sportList)
            }
        }.asFlow()

    override fun getFavoriteSport(): kotlinx.coroutines.flow.Flow<List<Sport>> {
        return localDataSource.getFavoriteSport().map{ DataMapper.mapEntitiesToDomain(it)}
    }

    override fun setFavoriteSport(sport: Sport, state: Boolean) {
        val sportEntity = DataMapper.mapDomainToEntity(sport)
        appExecutors.diskIO().execute { localDataSource.setFavoriteSport(sportEntity, state) }
    }
}

