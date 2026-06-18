import request from '@/utils/request'

export const useRegisterService = async (registerData)=>{
   const params = new URLSearchParams()
   for(let key in registerData){
    params.append(key,registerData[key]);
   }
   return await request.post('/user/register',params);
}

export const useLoginService = async (loginData) => {
   const params = new URLSearchParams()
   for(let key in loginData){
    params.append(key,loginData[key]);
   }
   return await request.post('/user/login',params);
}

export const getUserProfile = async () => {
   return await request.get('/user/profile');
}

export const updateUserProfile = async (profileData) => {
   const params = new URLSearchParams()
   for(let key in profileData){
    params.append(key,profileData[key]);
   }
   return await request.post('/user/updateProfile',params);
}

export const checkUserProfileComplete = async () => {
   return await request.get('/user/checkProfileComplete');
}

// 修改密码
export const updatePassword = async (passwordData) => {
   return await request.patch('/user/updatePwd', passwordData, {
      headers: { 'Content-Type': 'application/json' }
   });
}

export const uploadAvatar = async (file)=>{
   const formData = new FormData();
   formData.append('file',file);
   return await request.post('/user/uploadAvatar',formData,{
      headers:{'Content-Type':'multipart/form-data'}
   });
}

export const deleteAccount = async ()=>{
   return await request.delete('/user/deleteAccount');
}
