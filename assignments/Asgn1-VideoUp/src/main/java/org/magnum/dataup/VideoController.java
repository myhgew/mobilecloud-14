/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;

@Controller
public class VideoController {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|

	 * 
	 */

    private Map<Long,Video> videos = new HashMap<Long, Video>();

	private static final AtomicLong currentId = new AtomicLong(0L);
    private VideoFileManager videoDataMgr;

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		return videos.values();
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		return save(v);
	}
	
	// TODO
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public VideoStatus setVideoData(@Param(VideoSvcApi.ID_PARAMETER) Long id, @RequestParam(VideoSvcApi.DATA_PARAMETER) TypedFile videoData){
		VideoStatus videoStatus = new VideoStatus(VideoStatus.VideoState.READY);
		return videoStatus;
	}
	
	// TODO
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public Response getData(@Param(VideoSvcApi.ID_PARAMETER) Long id){
		Video v = videos.get(id);
		List<Header> headers = new ArrayList<Header>() {{ add(new Header("name", "value"));}};
		TypedInput body = null;
		Response response = null;
//		if(v == null) {
//			// Response(String url, int status, String reason, List<Header> headers, TypedInput body)
//			response = new Response(getDataUrl(id), 404, "Video not found", headers, null);
//		} else {
			response = new Response(getDataUrl(id), 200, "Video found", headers, null);
//		}
		return response;
	}
	
	// Helper methods
    private Video save(Video entity) {
        checkAndSetId(entity);
        videos.put(entity.getId(), entity);
        return entity;
    }

    private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }
    
    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
       HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       String base = "http://" + request.getServerName() + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
       return base;
    }
    
 // You would need some Controller method to call this...
    public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
         videoDataMgr.saveVideoData(v, videoData.getInputStream());
    }

    public void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
         // Of course, you would need to send some headers, etc. to the
         // client too!
         //  ...
         videoDataMgr.copyVideoData(v, response.getOutputStream());
    }
}
