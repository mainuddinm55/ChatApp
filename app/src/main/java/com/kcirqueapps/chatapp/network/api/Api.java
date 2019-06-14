package com.kcirqueapps.chatapp.network.api;

import com.kcirqueapps.chatapp.network.model.Chat;
import com.kcirqueapps.chatapp.network.model.Conversion;
import com.kcirqueapps.chatapp.network.model.File;
import com.kcirqueapps.chatapp.network.model.Friendship;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.GroupMember;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {
    @POST("registration")
    @FormUrlEncoded
    Single<HttpResponse<User>> registerUser(
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("email") String email,
            @Field("password") String password,
            @Field("dob") String dob,
            @Field("gender") String gender,
            @Field("mobile") String mobile
    );

    @POST("login")
    @FormUrlEncoded
    Single<HttpResponse<User>> login(
            @Field("email") String email,
            @Field("password") String password,
            @Field("lat") double lat,
            @Field("lng") double lng,
            @Field("token") String token
    );

    @POST("profilephotourl")
    @FormUrlEncoded
    Single<HttpResponse<User>> setProfileImageUrl(
            @Field("url") String url,
            @Field("id") int id
    );

    @GET("user/{id}")
    Single<HttpResponse<User>> getUser(
            @Path("id") int id
    );

    @GET("search/{id}")
    Observable<HttpResponse<List<User>>> searchUser(
            @Path("id") int id,
            @Query("search") String queryString
    );

    @POST("sendrequest")
    @FormUrlEncoded
    Single<HttpResponse<String>> sendRequest(
            @Field("from_id") int fromUserId,
            @Field("to_id") int toUserId
    );

    @GET("pendingrequest/{id}")
    Single<HttpResponse<List<User>>> pendingRequest(
            @Path("id") int id
    );

    @GET("friends/{id}")
    Single<HttpResponse<List<User>>> getFriends(
            @Path("id") int id
    );

    @GET("friendlist/{id}")
    Single<HttpResponse<List<User>>> friendList(
            @Path("id") int userId
    );

    @POST("friendshipstatus")
    @FormUrlEncoded
    Single<HttpResponse<Friendship>> friendshipStatus(
            @Field("from_id") int fromId,
            @Field("to_id") int toId
    );

    @PUT("acceptrequest")
    @FormUrlEncoded
    Single<HttpResponse> acceptRequest(
            @Field("request_from") int requestFromId,
            @Field("accept_from") int acceptFrom
    );

    @POST("singlechat")
    @FormUrlEncoded
    Single<HttpResponse<Chat>> singleChat(
            @Field("receiver_id") int receiverId,
            @Field("sender_id") int senderId,
            @Field("message") String message,
            @Field("message_type") String messageType,
            @Field("media_url") String mediaUrl
    );

    @GET("singlechat/{sender_id}/{receiver_id}")
    Single<HttpResponse<List<Chat>>> getSingleChat(
            @Path("sender_id") int senderId,
            @Path("receiver_id") int receiverId
    );

    @GET("conversionuser/{id}")
    Single<HttpResponse<List<User>>> getConversionUser(
            @Path("id") int id
    );

    @POST("groups")
    @FormUrlEncoded
    Single<HttpResponse<Group>> createGroup(
            @Field("name") String name,
            @Field("privacy") String privacy,
            @Field("creatorId") int creatorId
    );

    @GET("group/{id}")
    Single<HttpResponse<Group>> getGroup(
            @Path("id") int id
    );

    @POST("groupmember")
    @FormUrlEncoded
    Single<HttpResponse<GroupMember>> addGroupMember(
            @Field("group_id") int groupId,
            @Field("member_id") int memberId,
            @Field("add_by") int addedById
    );

    @GET("groups/{memberid}")
    Single<HttpResponse<List<Group>>> getGroups(
            @Path("memberid") int memberId
    );

    @GET("totalgroupmember/{groupid}")
    Single<HttpResponse<Integer>> getGroupMemberCount(
            @Path("groupid") int groupId
    );

    @GET("searchgroup/{id}")
    Observable<HttpResponse<List<Group>>> searchGroup(
            @Path("id") int id,
            @Query("name") String searchString
    );

    @POST("conversion")
    @FormUrlEncoded
    Single<HttpResponse> conversion(
            @Field("receiver_id") int receiverId,
            @Field("sender_id") int senderId,
            @Field("conversion_type") String conversionType,
            @Field("message") String message,
            @Field("media_url") String mediaUrl,
            @Field("file_name") String fileName,
            @Field("media_type") String mediaType
    );

    @GET("singleconversions/{sender_id}/{receiver_id}")
    Single<HttpResponse<List<Conversion>>> getSingleConversions(
            @Path("sender_id") int senderId,
            @Path("receiver_id") int receiverId,
            @Query("order_by") int orderBy,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("groupconversions/{group_id}")
    Single<HttpResponse<List<Conversion>>> getGroupConversions(
            @Path("group_id") int groupId,
            @Query("order_by") int orderBy,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("conversion/{id}")
    Single<HttpResponse<List<Chat>>> getChatList(
            @Path("id") int id
    );

    @POST("seenmessage/{id}")
    Single<HttpResponse> seenMessage(
            @Path("id") int id
    );

    @Multipart
    @POST("uploadfile")
    Single<HttpResponse<File>> uploadFile(
            @Part MultipartBody.Part file
    );

    @GET("nearby/{id}")
    Single<HttpResponse<List<User>>> getNearBy(
            @Path("id") int id,
            @Query("lat") double lat,
            @Query("lng") double lng
    );
}
