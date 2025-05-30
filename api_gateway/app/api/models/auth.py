from pydantic import BaseModel, Field
from datetime import datetime

class TokenData(BaseModel):
    user_id:int = Field(description="The id of the user either in the local or dataspace iam")
    user_role:str = Field(description="The role of the user either in the local or dataspace iam")
    expiration:datetime = Field(description="When the token expires.")

class TokenRequest(BaseModel):
    username: str
    password: str

class Tokens(BaseModel):
    accessToken:str = Field(description="The access token for the user")
    refreshToken:str = Field(description="The refresh token for the user")

